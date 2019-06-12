package integration

import org.junit.Test
import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import org.gradle.api.JavaVersion
import org.junit.BeforeClass


class JavaExecTest : AbstractGradleTest() {

    companion object {

        @BeforeClass
        @JvmStatic
        fun toolchainsRequirements() =
            assumeAvailableToolchains(JavaVersion.VERSION_12)
    }

    @Test
    fun `can use jvm with javaexec - groovy dsl`() {

        withBuildLogic(
            "build.gradle",
            """

                def java12 = javaToolchains.register("java12") {
                    version = JavaVersion.VERSION_12
                }

                tasks.register("exec", JavaExec) {
                    java12.get().configure(delegate)
                    classpath = sourceSets.main.output
                    main = "com.acme.Main"
                }

            """
        )

        println(rootDir.resolve("build.gradle").readText())

        build("exec", "-s") {
            assertOutputContains("javaExec:12")
        }
    }

    @Test
    fun `can use jvm with javaexec - kotlin dsl`() {

        withBuildLogic(
            "build.gradle.kts",
            """

                val java12 by javaToolchains.registering {
                    version.set(JavaVersion.VERSION_12)
                }

                tasks.register<JavaExec>("exec") {
                    java12.get().configure(this)
                    classpath = files(sourceSets.main.map { it.output })
                    main = "com.acme.Main"
                }

            """
        )

        build("exec", "-s") {
            assertOutputContains("javaExec:12")
        }
    }


    private
    fun withBuildLogic(buildScriptPath: String, buildLogic: String) {

        withSettings()
        withFile(
            buildScriptPath,
            """
                plugins {
                    id("java")
                    id("net.grdev.java-toolchains")
                }

                $buildLogic

            """
        )
        withFile("src/main/java/com/acme/Main.java", """
            package com.acme;

            public class Main {
                public static void main(String[] args) {
                    ${javaSourcePrintlnJavaVersion("javaExec")}
                }
            }
        """)
    }
}
