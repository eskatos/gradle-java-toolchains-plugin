package toolchains

import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import fixtures.assumeCurrentToolchain
import org.gradle.api.JavaVersion
import org.gradle.internal.jvm.Jvm
import org.junit.Test


// TODO assertions, including lifecycle
// TODO can report invalid toolchains
class ReportTest : AbstractGradleTest() {

    @Test
    fun toolchains() {

        assumeCurrentToolchain(JavaVersion.VERSION_1_8)
        assumeAvailableToolchains(
            JavaVersion.VERSION_1_8,
            JavaVersion.VERSION_1_10,
            JavaVersion.VERSION_12
        )

        withSettings()
        withBuildScript("""
            plugins {
                id("net.grdev.java-toolchains")
            }

            val java10 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_1_10)
            }
            val java12 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_12)
            }
            val java2 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_1_2)
            }
            val notFound by javaToolchains.registering {
                javaHome.set(layout.buildDirectory.dir("not-there"))
            }
            val invalidDir by javaToolchains.registering {
                javaHome.set(layout.projectDirectory)
            }
        """)

        build("javaToolchainsReport", "-s") {
            val jvm = Jvm.current()
            assertOutputContains("""
                gradle - Oracle JDK 8
                  ${jvm.javaHome}
            """)
            assertOutputContains("java12 - OpenJDK 12")
            assertOutputContains("java10 - Oracle JDK 10")
            assertOutputContains("java2 - INVALID")
        }
    }
}
