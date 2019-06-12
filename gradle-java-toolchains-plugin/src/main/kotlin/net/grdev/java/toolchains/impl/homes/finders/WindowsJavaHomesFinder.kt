package net.grdev.java.toolchains.impl.homes.finders

import net.grdev.java.toolchains.impl.homes.JavaHomesFinder
import net.rubygrapefruit.platform.MissingRegistryEntryException
import net.rubygrapefruit.platform.WindowsRegistry
import org.gradle.internal.nativeintegration.services.NativeServices
import java.io.File


internal
class WindowsJavaHomesFinder(
    private val keys: List<String>
) : JavaHomesFinder() {

    private
    val windowsRegistry =
        NativeServices.getInstance().get(WindowsRegistry::class.java)

    override fun find() = sequence {

        for (registryNode in keys) {

            val versions = try {
                windowsRegistry.getSubkeys(
                    WindowsRegistry.Key.HKEY_LOCAL_MACHINE,
                    registryNode
                )
            } catch (ex: MissingRegistryEntryException) {
                continue
            }

            for (version in versions) {
                if (version.matches("\\d+\\.\\d+".toRegex())) {
                    continue
                }
                try {
                    val path = windowsRegistry.getStringValue(
                        WindowsRegistry.Key.HKEY_LOCAL_MACHINE,
                        "$registryNode\\$version",
                        "JavaHome"
                    )
                    if (path.isNotEmpty()) {
                        val javaHome = File(path)
                        if (javaHome.hasBinJava()) {
                            yield(javaHome)
                        }
                    }
                } catch (ex: MissingRegistryEntryException) {
                    continue
                }
            }
        }
    }
}
