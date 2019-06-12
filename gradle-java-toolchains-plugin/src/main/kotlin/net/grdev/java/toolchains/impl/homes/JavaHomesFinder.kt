package net.grdev.java.toolchains.impl.homes

import java.io.File
import java.io.FileFilter


abstract class JavaHomesFinder {

    abstract fun find(): Sequence<File>

    protected
    fun File.listDirectories(): Array<out File> =
        listFiles(FileFilter { it.isDirectory })
            ?: emptyArray()

    protected
    fun File.hasBinJava() =
        binSlashJava().isFile
}
