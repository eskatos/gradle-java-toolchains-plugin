package net.grdev.java.toolchains.impl.homes

import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchainsDiscovery
import net.grdev.java.toolchains.impl.homes.finders.BaseDirsJavaHomesFinder
import net.grdev.java.toolchains.impl.homes.finders.MacOsJavaHomesFinder
import net.grdev.java.toolchains.impl.homes.finders.SdkManJavaHomesFinder
import net.grdev.java.toolchains.impl.homes.finders.WindowsJavaHomesFinder
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.os.OperatingSystem
import java.io.File


internal
fun discoverJavaHomes(spec: DefaultJavaToolchainsDiscovery, objects: ObjectFactory): List<File> {

    val os = OperatingSystem.current()
    val finders = mutableListOf<JavaHomesFinder>()
    if (spec.baseDirectories.enabled.orNull != false && !os.isWindows) {
        finders.add(BaseDirsJavaHomesFinder(
            spec.baseDirectories.directories.get().map { it.asFile }
        ))
    }
    if (spec.sdkMan.enabled.orNull != false && os.isLinux || os.isMacOsX) {
        finders.add(SdkManJavaHomesFinder)
    }
    if (spec.macosLibrary.enabled.orNull != false && os.isMacOsX) {
        finders.add(MacOsJavaHomesFinder(
            spec.macosLibrary.directories.get().map { it.asFile }
        ))
    }
    if (spec.windowsRegistry.enabled.orNull != false && os.isWindows) {
        finders.add(WindowsJavaHomesFinder(
            spec.windowsRegistry.keys.get()
        ))
    }
    finders.addAll(
        spec.finderInstances.get()
    )
    finders.addAll(
        spec.finderTypes.get().map { (type, ctorArguments) ->
            objects.newInstance(type, *ctorArguments.toTypedArray())
        }
    )
    return finders.flatMap { it.find().toList() }
}


internal
fun File.binSlashJava() =
    resolve(
        if (OperatingSystem.current().isWindows) "bin/java.exe"
        else "bin/java"
    )
