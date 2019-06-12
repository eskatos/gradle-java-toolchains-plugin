plugins {
    base
    id("com.gradle.plugin-publish") version "0.10.1" apply false
}

allprojects {

    group = "net.grdev.java-toolchains"
    version = "0"

    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

// TODO ktlint
