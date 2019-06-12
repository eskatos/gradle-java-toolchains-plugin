package net.grdev.java.toolchains.impl.homes.finders

import net.grdev.java.toolchains.impl.homes.JavaHomesFinder
import java.io.File

// TODO consider /usr/lib/java_exec -V
//  expect slower to fork a process
internal
class MacOsJavaHomesFinder(
    private val directories: List<File>
) : JavaHomesFinder() {

    override fun find() = sequence {
        for (jvmDir in directories) {
            for (dir in jvmDir.listDirectories()) {
                val home = dir.resolve("Contents/Home")
                if (home.hasBinJava()) {
                    yield(home)
                }
            }
        }
    }
}
