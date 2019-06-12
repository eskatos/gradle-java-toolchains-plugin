package net.grdev.java.toolchains.tasks

import net.grdev.java.toolchains.dsl.JavaToolchain
import net.grdev.java.toolchains.impl.dsl.JavaToolchainInternal
import net.grdev.java.toolchains.impl.dsl.internal
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin
import javax.inject.Inject


abstract class ValidateJavaToolchains @Inject constructor() : DefaultTask() {

    companion object {
        const val defaultTaskName = "validateJavaToolchains"
    }

    @get:Internal
    abstract val javaToolchains: ListProperty<JavaToolchain>

    init {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Validates registered Java toolchains."
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun validate() {
        val errors = mutableListOf<Pair<JavaToolchain, Exception>>()
        javaToolchains.get().forEach { spec ->
            val error = spec.internal.validationResult.get().error
            if (error != null) {
                errors.add(spec to error)
            }
        }
        if (errors.size == 1) {
            throw errors.single().second
        }
        if (errors.isNotEmpty()) {
            val message = "Invalid Java toolchains: ${errors.joinToString(", ") { it.first.name }}\n" +
                errors.mapNotNull { it.second.message }.joinToString("\n", "- ").prependIndent("  ")
            throw IllegalStateException(message).also { ex ->
                errors.forEach { ex.addSuppressed(it.second) }
            }
        }
    }
}
