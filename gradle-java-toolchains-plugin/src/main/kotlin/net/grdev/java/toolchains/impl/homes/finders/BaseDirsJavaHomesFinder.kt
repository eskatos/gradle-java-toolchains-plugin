package net.grdev.java.toolchains.impl.homes.finders

import net.grdev.java.toolchains.impl.homes.JavaHomesFinder
import java.io.File


internal
open class BaseDirsJavaHomesFinder(
    private val baseDirs: List<File>
) : JavaHomesFinder() {

    override fun find() = sequence {
        for (baseDir in baseDirs) {
            for (dir in baseDir.listDirectories()) {
                if (dir.hasBinJava()) {
                    yield(dir)
                }
            }
        }
    }
}
