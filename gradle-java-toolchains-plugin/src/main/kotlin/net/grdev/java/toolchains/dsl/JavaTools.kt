package net.grdev.java.toolchains.dsl

import org.gradle.api.Named
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider


interface JavaTools : Map<String, JavaTool> {

    val java: JavaTool
    val keytool: JavaTool
    val pack200: JavaTool
    val unpack200: JavaTool
}


interface JavaTool : Named {

    val description: Provider<String>
    val executable: Provider<RegularFile>
}

