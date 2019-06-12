package net.grdev.java.toolchains.impl.homes.finders

import java.io.File


internal
object SdkManJavaHomesFinder : BaseDirsJavaHomesFinder(
    listOf(File("${
    System.getenv("SDKMAN_CANDIDATES_DIR")
        ?: "${System.getProperty("user.home")}/.sdkman/candidates"
    }/java"))
)
