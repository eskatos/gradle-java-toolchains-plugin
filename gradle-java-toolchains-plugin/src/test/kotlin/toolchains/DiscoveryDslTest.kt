package toolchains

import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import org.gradle.api.JavaVersion
import org.junit.Test


// TODO assertions, including lifecycle
class DiscoveryDslTest : AbstractGradleTest() {

    @Test
    fun `can configure discovery`() {

        assumeAvailableToolchains(JavaVersion.VERSION_12)
        withSettings()

        withBuildScript("""
            plugins {
                id("net.grdev.java-toolchains")
            }

            javaToolchainsDiscovery {
                baseDirectories {
                    enabled.set(false)
                }
                sdkMan {
                    enabled.set(true)
                }
                macosLibrary {
                    enabled.set(false)
                }
                windowsRegistry {
                    enabled.set(false)
                }
            }

            val java12 by javaToolchains.registering {
                jdk.set(true)
                version.set(JavaVersion.VERSION_12)
            }

            tasks.register("some") {
                doLast {
                    println(java12.get().version.get())
                    println(java12.get().tools.java.executable.get()) // triggers probe, validation & freeze
                }
            }
        """)

        build("some", "-s")
    }
}
