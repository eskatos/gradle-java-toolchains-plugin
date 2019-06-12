package net.grdev.java.toolchains.impl.dsl


internal
data class JavaToolchainValidationResult(
    val error: Exception? = null
) {
    val isValid
        get() = error == null

    companion object {
        val VALID = JavaToolchainValidationResult()
    }
}
