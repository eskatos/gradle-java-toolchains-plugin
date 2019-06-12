package net.grdev.java.toolchains.tasks

import net.grdev.java.toolchains.dsl.JavaToolchain
import net.grdev.java.toolchains.dsl.JavaToolchainsContainer
import net.grdev.java.toolchains.impl.dsl.JavaToolchainInternal
import net.grdev.java.toolchains.impl.dsl.internal
import net.grdev.java.toolchains.impl.homes.JavaHome
import net.grdev.java.toolchains.impl.homes.JavaHomes
import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.HelpTasksPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.text.DefaultTextReportBuilder
import org.gradle.api.tasks.diagnostics.internal.text.TextReportBuilder
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.reporting.ReportRenderer
import javax.inject.Inject


abstract class JavaToolchainsReport @Inject internal constructor(
    private val toolchains: JavaToolchainsContainer,
    private val homes: JavaHomes,
    private val textOutputFactory: StyledTextOutputFactory,
    private val fileResolver: FileResolver
) : DefaultTask() {

    companion object {
        const val defaultTaskName = "javaToolchainsReport"
    }

    init {
        group = HelpTasksPlugin.HELP_GROUP
        description = "Prints a report of registered and discovered Java toolchains."
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun report() {

        // eagerly trigger discovery and probing in order to not mix logging
        toolchains.forEach { it.internal.validationResult.get() }

        textReportBuilder.apply {

            heading("Java toolchains")

            subheading("Registered Java toolchains")
            if (toolchains.isEmpty()) output.appendln().appendln("No Java toolchain registered.")
            else collection(toolchains.sortedByDescending { it.version.takeIf { it.isPresent }?.get() }, RegisteredRenderer)

            output.println()
            subheading("Discovered Java homes")
            if (homes.availableJavaHomes.isEmpty()) output.appendln().appendln("No Java home discovered.")
            else collection(homes.availableJavaHomes.sortedByDescending { it.version }, DiscoveredRenderer)
        }
    }

    private
    val textReportBuilder: TextReportBuilder
        get() = DefaultTextReportBuilder(
            textOutputFactory.create(JavaToolchainsReport::class.java),
            fileResolver
        )
}


private
object RegisteredRenderer : ReportRenderer<JavaToolchain, TextReportBuilder>() {

    override fun render(model: JavaToolchain, output: TextReportBuilder) {

        val identifier = model.name
        lateinit var description: String
        lateinit var detail: String

        val error = model.internal.validationResult.get().error
        if (error == null) {
            description = model.description.get()
            detail = model.javaHome.get().asFile.path
        } else {
            description = "INVALID"
            detail = error.message.toString()
        }

        output.output.apply {
            println()
            withStyle(StyledTextOutput.Style.Identifier)
            append(identifier)
            withStyle(StyledTextOutput.Style.Normal)
            append(" - ").append(description).appendln()
            append("  ").append(detail).appendln()
        }
    }
}


private
object DiscoveredRenderer : ReportRenderer<JavaHome.Valid, TextReportBuilder>() {

    override fun render(model: JavaHome.Valid, output: TextReportBuilder) {
        output.output.apply {
            println()
            withStyle(StyledTextOutput.Style.Identifier)
            append(model.description).appendln()
            withStyle(StyledTextOutput.Style.Normal)
            append("  ").append(model.dir.path).appendln()
        }
    }
}
