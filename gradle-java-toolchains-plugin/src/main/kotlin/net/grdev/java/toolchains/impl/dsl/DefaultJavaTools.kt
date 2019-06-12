package net.grdev.java.toolchains.impl.dsl

import net.grdev.java.toolchains.dsl.JavaTool
import net.grdev.java.toolchains.dsl.JavaTools
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider


internal
class DefaultJavaTools(tools: Map<String, JavaTool>) : JavaTools, Map<String, JavaTool> by tools {

    override val java: JavaTool
        get() = getValue("java")

    override val keytool: JavaTool
        get() = getValue("keytool")

    override val pack200: JavaTool
        get() = getValue("pack200")

    override val unpack200: JavaTool
        get() = getValue("unpack200")
}


internal
class DefaultJavaTool(
    private val name: String,
    override val description: Provider<String>,
    override val executable: Provider<RegularFile>
) : JavaTool {

    override fun getName(): String = name
}
