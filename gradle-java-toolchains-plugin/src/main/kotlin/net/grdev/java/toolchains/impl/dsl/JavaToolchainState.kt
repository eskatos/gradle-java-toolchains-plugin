package net.grdev.java.toolchains.impl.dsl

import org.gradle.api.JavaVersion
import java.io.File


internal
sealed class JavaToolchainState {

    abstract val description: String

    data class Valid(
        val home: File,
        val version: JavaVersion,
        val isJdk: Boolean,
        override val description: String
    ) : JavaToolchainState()

    data class Invalid(
        val error: Exception,
        val home: File?,
        val version: JavaVersion?,
        val isJdk: Boolean?,
        override val description: String
    ) : JavaToolchainState() {

        fun rethrow(): Nothing =
            throw error
    }
}
