package integration

import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import org.gradle.api.JavaVersion
import org.junit.Test


// TODO add missing assertions, incl
class JavaPluginTest : AbstractGradleTest() {

    @Test
    fun `can configure java plugin tasks`() {

        assumeAvailableToolchains(
            JavaVersion.VERSION_1_8,
            JavaVersion.VERSION_1_10,
            JavaVersion.VERSION_12
        )

        withSettings()
        withBuildScript("""
            plugins {
                java
                id("net.grdev.java-toolchains")
            }

            java {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            repositories { jcenter() }
            dependencies { testImplementation("junit:junit:4.12") }

            val java8 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_1_8)
            }
            val java10 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_1_10)
            }
            val java12 by javaToolchains.registering {
                version.set(JavaVersion.VERSION_12)
            }

            tasks {
                compileJava {
                    java10.get().configure(this)
                }
                compileTestJava {
                    java10.get().configure(this)
                }
                test {
                    java8.get().configure(this)
                    testLogging.showStandardStreams = true
                }
                javadoc {
                    java12.get().configure(this)
                }
            }
        """)

        withFile("src/main/java/Thing.java", """
            public class Thing {}
        """)
        withFile("src/test/java/ThingTest.java", """
            import org.junit.*;
            public class ThingTest {
                @Test
                public void test() {
                    ${javaSourcePrintlnJavaVersion("test")}
                }
            }
        """)

        build("test", "javadoc") {
            assertOutputContains("test:1.8")
        }
    }
}
