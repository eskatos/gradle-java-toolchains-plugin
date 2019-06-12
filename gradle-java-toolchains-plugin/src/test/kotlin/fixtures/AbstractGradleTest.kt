package fixtures

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File


abstract class AbstractGradleTest {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    protected
    val rootDir: File
        get() = temporaryFolder.root


    protected
    fun withSettings(text: String = "") =
        withFile("settings.gradle.kts", text)

    protected
    fun withBuildScript(text: String = "") =
        withFile("build.gradle.kts", text)

    protected
    fun withFile(path: String, text: String = "") =
        rootDir.resolve(path).also {
            it.parentFile.mkdirs()
            it.writeText(text.trimIndent())
        }

    protected
    fun javaSourcePrintlnJavaVersion(tag: String) =
        """System.out.println("$tag:" + System.getProperty("java.version"));"""

    protected
    fun build(vararg arguments: String, block: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunner()
            .withArguments(*(extraArguments + arguments).toTypedArray())
            .build()
            .apply {
                println(output)
                block()
            }

    protected
    fun buildAndFail(vararg arguments: String, block: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunner()
            .withArguments(*(extraArguments + arguments).toTypedArray())
            .buildAndFail()
            .apply {
                println(output)
                block()
            }

    protected
    open val extraArguments: List<String> =
        emptyList()

    protected
    fun gradleRunner(): GradleRunner =
        GradleRunner.create()
            .withProjectDir(temporaryFolder.root)
            .withPluginClasspath()

    protected
    fun BuildResult.assertOutputContains(string: String) =
        assertThat(
            output,
            containsString(string.trimIndent())
        )
}

