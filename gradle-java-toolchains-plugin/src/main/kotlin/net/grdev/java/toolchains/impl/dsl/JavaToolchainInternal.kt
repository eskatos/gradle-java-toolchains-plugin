package net.grdev.java.toolchains.impl.dsl

import net.grdev.java.toolchains.dsl.JavaToolchain
import org.gradle.api.provider.Provider


internal
interface JavaToolchainInternal {
    val validationResult: Provider<JavaToolchainValidationResult>
}


internal
val JavaToolchain.internal: JavaToolchainInternal
    get() = this as JavaToolchainInternal
