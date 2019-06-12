package net.grdev.java.toolchains.dsl

import net.grdev.java.toolchains.impl.homes.JavaHomesFinder
import org.gradle.api.Action
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject
import kotlin.reflect.KClass


abstract class JavaToolchainsDiscovery protected constructor() {

    abstract val baseDirectories: BaseDirectoriesSpec

    fun baseDirectories(action: Action<BaseDirectoriesSpec>) =
        action.execute(baseDirectories)

    abstract val sdkMan: SdkManSpec

    fun sdkMan(action: Action<SdkManSpec>) =
        action.execute(sdkMan)

    abstract val macosLibrary: MacOsLibrarySpec

    fun macosLibrary(action: Action<MacOsLibrarySpec>) =
        action.execute(macosLibrary)

    abstract val windowsRegistry: WindowsRegistrySpec

    fun windowsRegistry(action: Action<WindowsRegistrySpec>) =
        action.execute(windowsRegistry)

    abstract fun registerFinder(type: Class<JavaHomesFinder>, vararg ctorArguments: Any)

    fun registerFinder(type: KClass<JavaHomesFinder>, vararg ctorArguments: Any) =
        registerFinder(type.java, *ctorArguments)

    inline
    fun <reified T : JavaHomesFinder> registerFinder(vararg ctorArguments: Any): Unit =
        registerFinder(T::class.java as Class<JavaHomesFinder>, *ctorArguments)

    abstract fun registerFinder(finder: JavaHomesFinder)
}


// TODO extract iface
abstract class BaseDirectoriesSpec @Inject constructor(
    private val layout: ProjectLayout
) {

    abstract val enabled: Property<Boolean>

    abstract val directories: ListProperty<FileSystemLocation>

    companion object {

        private
        val CONVENTIONAL_DIRECTORIES = listOf(
            "/usr/lib/jvm", // *deb, Arch
            "/opt", // *rpm, Gentoo, HP/UX
            "/usr/lib", // Slackware 32
            "/usr/lib64", // Slackware 64
            "/usr/local", // OpenBSD, FreeBSD
            "/usr/pkg/java", // NetBSD
            "/usr/jdk/instances" // Solaris
        )
    }

    init {
        directories.convention(
            CONVENTIONAL_DIRECTORIES.map { layout.projectDirectory.dir(it) }
        )
    }
}


// TODO extract iface
abstract class SdkManSpec @Inject constructor() {

    abstract val enabled: Property<Boolean>
}


// TODO extract iface
abstract class MacOsLibrarySpec @Inject constructor(
    private val layout: ProjectLayout
) {

    abstract val enabled: Property<Boolean>

    abstract val directories: ListProperty<FileSystemLocation>

    companion object {

        private
        val CONVENTIONAL_DIRECTORY = "/Library/Java/JavaVirtualMachines"
    }

    init {
        directories.convention(listOf(
            layout.projectDirectory.dir(CONVENTIONAL_DIRECTORY)
        ))
    }
}


// TODO extract iface
abstract class WindowsRegistrySpec @Inject constructor() {

    abstract val enabled: Property<Boolean>

    abstract val keys: ListProperty<String>

    companion object {

        private
        val CONVENTIONAL_REGISTRY_NODES = listOf(
            "SOFTWARE\\JavaSoft\\JDK",
            "SOFTWARE\\JavaSoft\\JRE",
            "SOFTWARE\\JavaSoft\\Java Development Kit",
            "SOFTWARE\\JavaSoft\\Java Runtime Environment",
            "SOFTWARE\\Wow6432Node\\JavaSoft\\Java Development Kit",
            "SOFTWARE\\Wow6432Node\\JavaSoft\\Java Runtime Environment",
            "SOFTWARE\\IBM\\Java2 Runtime Environment"
        )
    }

    init {
        keys.convention(CONVENTIONAL_REGISTRY_NODES)
    }
}
