package toolchains

import fixtures.AbstractGradleTest
import org.gradle.internal.jvm.Jvm
import org.junit.Test


class ValidationTest : AbstractGradleTest() {

    @Test
    fun `can validate registered java toolchains`() {

        withSettings()
        withBuildScript("""
            plugins {
                id("net.grdev.java-toolchains")
            }

            javaToolchains {
                gradle {
                    version.set(JavaVersion.VERSION_1_1)
                }
                register("java2") {
                    version.set(JavaVersion.VERSION_1_2)
                }
            }

        """)

        buildAndFail("validateJavaToolchains") {
            val jvm = Jvm.current()
            // TODO review error messages and refine assertions
            assertOutputContains("Invalid Java toolchains: gradle, java2")
            assertOutputContains("Invalid Java toolchain 'gradle' in '${jvm.javaHome}'")
            assertOutputContains("version is not 1.1 but ${jvm.javaVersion}")
            assertOutputContains("No Java JDK 1.2 toolchain found, please")
        }
    }
}
