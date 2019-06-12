package samples

import fixtures.AbstractGradleTest
import fixtures.assumeAvailableToolchains
import fixtures.availableToolchains
import fixtures.currentToolchain
import org.gradle.api.JavaVersion
import org.junit.BeforeClass
import org.junit.Test
import java.io.File


class SamplesTest : AbstractGradleTest() {

    companion object {

        @BeforeClass
        @JvmStatic
        fun toolchainsRequirements() =
            assumeAvailableToolchains(
                JavaVersion.VERSION_1_8,
                JavaVersion.VERSION_1_10,
                JavaVersion.VERSION_12
            )
    }

    @Test
    fun `cross-version-testing groovy-dsl`() {

        println(currentToolchain)
        println(availableToolchains)

        withSample("cross-version-testing/groovy")
        build("check") {
            assertOutputContains("test:1.8")
            assertOutputContains("test:10")
            assertOutputContains("test:12")
        }
    }

    @Test
    fun `java-exec kotlin-dsl`() {

        withSample("java-exec/kotlin")
        build("javaExec", "eagerJavaExec") {
            assertOutputContains("javaExec:12")
            assertOutputContains("eagerJavaExec:10")
        }
    }

    @Test
    fun `java-exec groovy-dsl`() {

        withSample("java-exec/groovy")
        build("javaExec", "eagerJavaExec") {
            assertOutputContains("javaExec:12")
            assertOutputContains("eagerJavaExec:10")
        }
    }

    private
    fun withSample(path: String) {
        File("src/samples/gradle/$path").copyRecursively(rootDir)
    }

    override val extraArguments: List<String> =
        listOf(
            "-I",
            File("src/samples/gradle/testing-samples.init.gradle.kts").absolutePath
        )
}
