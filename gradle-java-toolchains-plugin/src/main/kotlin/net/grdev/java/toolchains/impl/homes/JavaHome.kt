package net.grdev.java.toolchains.impl.homes

import org.gradle.api.JavaVersion
import java.io.File


internal
sealed class JavaHome {

    abstract val dir: File
    abstract val description: String

    data class Valid(
        override val dir: File,
        val isJdk: Boolean,
        val version: JavaVersion,
        override val description: String
    ) : JavaHome()

    data class Invalid(
        override val dir: File,
        override val description: String
    ) : JavaHome()
}
