package net.grdev.java.toolchains.dsl

import org.gradle.api.NamedDomainObjectContainer


interface JavaToolchainsContainer : NamedDomainObjectContainer<JavaToolchain> {

    companion object {
        const val defaultExtensionName = "javaToolchains"
    }
}
