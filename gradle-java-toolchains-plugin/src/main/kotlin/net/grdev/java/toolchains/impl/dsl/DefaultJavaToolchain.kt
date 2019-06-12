package net.grdev.java.toolchains.impl.dsl


import net.grdev.java.toolchains.dsl.JavaTool
import net.grdev.java.toolchains.dsl.JavaToolchain
import net.grdev.java.toolchains.dsl.JavaTools
import net.grdev.java.toolchains.impl.dsl.JavaToolchainState.Invalid
import net.grdev.java.toolchains.impl.dsl.JavaToolchainState.Valid
import net.grdev.java.toolchains.impl.homes.JavaHome
import net.grdev.java.toolchains.impl.homes.JavaHomes
import org.gradle.api.JavaVersion
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.FileFilter
import javax.inject.Inject


@Suppress("UnstableApiUsage")
internal
abstract class DefaultJavaToolchain @Inject internal constructor(

    private val name: String,

    private val homes: JavaHomes,

    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val layout: ProjectLayout

) : JavaToolchain, JavaToolchainInternal {

    private
    val lazyState: JavaToolchainState by lazy {
        computeToolchainState()
    }

    private
    val state: Provider<JavaToolchainState> =
        providers.provider { lazyState }

    private
    val javaHomeProperty: DirectoryProperty =
        objects.directoryProperty()

    private
    val jdkProperty: Property<Boolean> =
        objects.property()

    private
    val versionProperty: Property<JavaVersion> =
        objects.property()

    override val javaHome: DirectoryProperty =
        ComputedDirectoryProperty(javaHomeProperty, state.map {
            when (it) {
                is Valid -> layout.projectDirectory.dir(it.home.path)
                is Invalid -> javaHomeProperty.get()
            }
        })

    override val jdk: Property<Boolean> =
        ComputedProperty(jdkProperty, state.map {
            when (it) {
                is Valid -> it.isJdk
                is Invalid -> jdkProperty.get()
            }
        })

    override val version: Property<JavaVersion> =
        ComputedProperty(versionProperty, state.map {
            when (it) {
                is Valid -> it.version
                is Invalid -> versionProperty.get()
            }
        })

    abstract override val minimumVersion: Property<JavaVersion>

    abstract override val maximumVersion: Property<JavaVersion>

    init {
        jdkProperty.convention(true)
    }

    override fun getName(): String = name

    override fun toString(): String {
        val jdkString = jdkProperty.takeIf { it.isPresent }?.get()?.let { if (it) " JDK" else " JRE" } ?: ""
        val homeString = javaHomeProperty.takeIf { it.isPresent }?.get()?.let { " in '$it'" } ?: ""
        return if (versionProperty.isPresent) "Java$jdkString ${versionProperty.get()}$homeString"
        else {
            val min = minimumVersion.orNull
            val max = maximumVersion.orNull
            if (min != null && max != null) "Java$jdkString >= $min and <= $max$homeString"
            else if (min != null) "Java$jdkString >= $min$homeString"
            else if (max != null) "Java$jdkString <= $max$homeString"
            else "Java$jdkString$homeString"
        }
    }


    // Things that require finalized state, discovery, probing and validation

    private
    val toolsProvider: Provider<JavaTools> =
        state.map {
            when (it) {
                is Valid -> DefaultJavaTools(
                    it.home.resolve("bin")
                        .listFiles(FileFilter { it.isFile && it.canExecute() })
                        .map { executableFile ->
                            executableFile.name to newJavaTool(executableFile, it.description)
                        }
                        .toMap()
                )
                is Invalid -> DefaultJavaTools(emptyMap())
            }
        }

    override val tools: JavaTools
        get() = toolsProvider.get()

    override val description: Provider<String> =
        state.map { it.description }

    override val validationResult: Provider<JavaToolchainValidationResult> =
        state.map {
            when (it) {
                is Invalid -> JavaToolchainValidationResult(it.error)
                is Valid -> JavaToolchainValidationResult.VALID
            }
        }

    private
    fun computeToolchainState(): JavaToolchainState {

        val state =
            if (javaHomeProperty.isPresent) probeJavaHome(javaHomeProperty.get())
            else discoverFromSpec()

        applyToProperties(state)
        finalizeProperties()

        return state
    }

    private
    fun applyToProperties(state: JavaToolchainState) =
        when (state) {
            is Invalid -> {
                state.home?.let { javaHomeProperty.set(layout.projectDirectory.dir(it.absolutePath)) }
                state.isJdk?.let { jdkProperty.set(it) }
                state.version?.let { versionProperty.set(it) }
            }
            is Valid -> {
                javaHomeProperty.set(layout.projectDirectory.dir(state.home.absolutePath))
                jdkProperty.set(state.isJdk)
                versionProperty.set(state.version)
            }
        }

    private
    fun finalizeProperties() {
        javaHomeProperty.finalizeValue()
        jdkProperty.finalizeValue()
        versionProperty.finalizeValue()
        minimumVersion.finalizeValue()
        maximumVersion.finalizeValue()
    }

    private
    fun probeJavaHome(directory: Directory): JavaToolchainState =
        when (val home = homes.javaHome(directory.asFile)) {
            is JavaHome.Invalid -> Invalid(
                IllegalStateException(home.description),
                home.dir,
                null, null,
                "Invalid Java home"
            )
            is JavaHome.Valid -> {
                val violations = mutableListOf<String>()
                if (jdkProperty.isPresent && jdkProperty.get() != home.isJdk) {
                    violations.add("is not a ${if (jdkProperty.get()) "JDK" else "JRE"}")
                }
                if (versionProperty.isPresent && home.version != versionProperty.get()) {
                    violations.add("version is not ${versionProperty.get()} but ${home.version}")
                } else {
                    if (minimumVersion.isPresent && home.version < minimumVersion.get()) {
                        violations.add("version ${home.version} is lower than the minimum ${minimumVersion.get()}")
                    }
                    if (maximumVersion.isPresent && home.version > maximumVersion.get()) {
                        violations.add("version ${home.version} is higher than the maximum ${maximumVersion.get()}")
                    }
                }
                if (violations.isNotEmpty()) Invalid(
                    IllegalStateException(
                        "Invalid Java toolchain '$name' in '${home.dir}'\n" +
                            violations.joinToString("\n  - ", "  - ")
                    ),
                    home.dir,
                    null, null,
                    "Invalid Java toolchain"
                )
                else Valid(
                    home.dir,
                    home.version,
                    home.isJdk,
                    home.description
                )
            }
        }

    // TODO change from first matching to closest version selection
    private
    fun discoverFromSpec(): JavaToolchainState {
        val found = homes.availableJavaHomes.firstOrNull {
            if (jdkProperty.isPresent && jdkProperty.get() != it.isJdk) {
                false
            } else if (versionProperty.isPresent && versionProperty.get() != it.version) {
                false
            } else {
                if (minimumVersion.isPresent && it.version < minimumVersion.get()) {
                    return@firstOrNull false
                }
                if (maximumVersion.isPresent && it.version > maximumVersion.get()) {
                    return@firstOrNull false
                }
                true
            }
        }
        return if (found == null) Invalid(
            IllegalStateException("No $this toolchain found, please register a compatible JVM or install in a conventional location"),
            null, null, null,
            "Invalid Java toolchain '$name'"
        )
        else Valid(
            found.dir,
            found.version,
            found.isJdk,
            found.description
        )
    }

    private
    fun newJavaTool(executableFile: File, toolchainDisplayName: String): JavaTool =
        DefaultJavaTool(
            executableFile.name,
            providers.provider { "'${executableFile.name}' $toolchainDisplayName" },
            layout.file(providers.provider { executableFile })
        )

    override fun configure(javaCompile: JavaCompile) =
        configure(javaCompile.options)

    override fun configure(groovyCompile: GroovyCompile) =
        configure(groovyCompile.options)

    override fun configure(scalaCompile: ScalaCompile) =
        configure(scalaCompile.options)

    private
    fun configure(options: CompileOptions) {
        val javac by tools
        options.forkOptions.javaHome = javaHome.get().asFile
        options.forkOptions.executable = javac.executable.get().asFile.absolutePath
    }

    override fun configure(javaExec: JavaExec) {
        javaExec.executable(tools.java.executable.get().asFile)
    }

    override fun configure(javadoc: Javadoc) {
        javadoc.executable = tools.getValue("javadoc").executable.get().asFile.path
    }

    override fun configure(test: Test) {
        test.executable(tools.java.executable.get().asFile)
    }
}
