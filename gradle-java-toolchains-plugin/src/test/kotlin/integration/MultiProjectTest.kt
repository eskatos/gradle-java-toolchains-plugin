package integration

import fixtures.AbstractGradleTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class MultiProjectTest : AbstractGradleTest() {

    @Test
    fun `shares toolchains with parent projects`() {

        withSettings("""
            include("a")
            include("b")
        """)
        withBuildScript("""
            plugins {
                id("net.grdev.java-toolchains")
            }

            println(javaToolchains)

            subprojects {
                apply(plugin = "net.grdev.java-toolchains")
            }
        """)
        withFile("a/build.gradle.kts", """
            println(javaToolchains)
        """)
        withFile("b/build.gradle.kts", """
            println(javaToolchains)
        """)

        build("validateJavaToolchains") {
            assertThat(
                tasks.map { it.path }.filter { it.endsWith("validateJavaToolchains") },
                equalTo(listOf(":validateJavaToolchains"))
            )
        }
    }
}
