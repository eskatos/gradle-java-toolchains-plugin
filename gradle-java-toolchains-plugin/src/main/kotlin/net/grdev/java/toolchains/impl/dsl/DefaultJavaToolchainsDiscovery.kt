package net.grdev.java.toolchains.impl.dsl

import net.grdev.java.toolchains.dsl.*
import net.grdev.java.toolchains.impl.homes.JavaHomesFinder
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal
abstract class DefaultJavaToolchainsDiscovery @Inject constructor(
    objects: ObjectFactory
) : JavaToolchainsDiscovery() {

    override val baseDirectories: BaseDirectoriesSpec =
        objects.newInstance(BaseDirectoriesSpec::class)

    override val sdkMan: SdkManSpec =
        objects.newInstance(SdkManSpec::class)

    override val macosLibrary: MacOsLibrarySpec =
        objects.newInstance(MacOsLibrarySpec::class)

    override val windowsRegistry: WindowsRegistrySpec =
        objects.newInstance(WindowsRegistrySpec::class)

    data class FinderTypeSpec(
        val type: Class<JavaHomesFinder>,
        val ctorArguments: List<Any>
    )

    abstract val finderTypes: ListProperty<FinderTypeSpec>

    abstract val finderInstances: ListProperty<JavaHomesFinder>

    override fun registerFinder(type: Class<JavaHomesFinder>, vararg ctorArguments: Any) {
        finderTypes.add(FinderTypeSpec(type, ctorArguments.toList()))
    }

    override fun registerFinder(finder: JavaHomesFinder) {
        finderInstances.add(finder)
    }

    fun finalizeValues() {
        baseDirectories {
            enabled.finalizeValue()
            directories.finalizeValue()
        }
        sdkMan {
            enabled.finalizeValue()
        }
        macosLibrary {
            enabled.finalizeValue()
            directories.finalizeValue()
        }
        windowsRegistry {
            enabled.finalizeValue()
            keys.finalizeValue()
        }
        finderTypes.finalizeValue()
        finderInstances.finalizeValue()
    }
}
