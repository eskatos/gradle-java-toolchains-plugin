package net.grdev.java.toolchains.dsl

import org.gradle.api.JavaVersion
import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.api.tasks.testing.Test


interface JavaToolchain : Named {

    // Configurable properties

    val javaHome: DirectoryProperty
    val jdk: Property<Boolean>
    val version: Property<JavaVersion>
    val minimumVersion: Property<JavaVersion>
    val maximumVersion: Property<JavaVersion>

    // Read-only properties
    // Usage requires discovery, probing, validation and freezes configurable properties

    val description: Provider<String>
    val tools: JavaTools

    // Configuration helpers

    fun configure(javaCompile: JavaCompile)
    fun configure(groovyCompile: GroovyCompile)
    fun configure(scalaCompile: ScalaCompile)
    fun configure(javaExec: JavaExec)
    fun configure(javadoc: Javadoc)
    fun configure(test: Test)
}
