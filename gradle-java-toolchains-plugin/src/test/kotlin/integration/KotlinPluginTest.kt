package integration

import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import org.gradle.api.JavaVersion
import org.junit.Test


class KotlinPluginTest  : AbstractGradleTest() {

    @Test
    fun `kotlin plugin`() {

        assumeAvailableToolchains(JavaVersion.VERSION_1_10)

        withSettings()
        withBuildScript( """
            plugins {
                kotlin("jvm") version "1.3.31"
                application
                id("net.grdev.java-toolchains")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation(kotlin("stdlib"))
            }

            val java10 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_1_10)
            }

            tasks {
                compileKotlin {
                    kotlinOptions {
                        jdkHome = java10.get().javaHome.get().asFile.absolutePath
                    }
                }
                test {
                    java10.get().configure(this)
                }
            }

        """)
        withFile("src/main/kotlin/Main.kt", """
            fun main(args: Array<String>) {
                println("${'$'}{args.first()}:${'$'}{System.getProperty("java.version")}")
            }
        """)

        build("check")
    }
}
