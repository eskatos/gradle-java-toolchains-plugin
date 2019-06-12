package fixtures

import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchainsDiscovery
import net.grdev.java.toolchains.impl.homes.JavaHomeProbe
import net.grdev.java.toolchains.impl.homes.JavaHomes
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.impldep.com.google.common.io.Files
import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.junit.Assume.assumeThat
import java.io.File


data class TestToolchain(val home: File, val version: JavaVersion, val isJdk: Boolean)


val currentToolchain =
    Jvm.current().let { current ->
        TestToolchain(current.javaHome, current.javaVersion!!, current.javacExecutable != null)
    }


val availableToolchains by lazy {
    ProjectBuilder
        .builder()
        .withProjectDir(Files.createTempDir())
        .build()
        .serviceOf<ObjectFactory>().run {
            newInstance(
                JavaHomes::class,
                newInstance(DefaultJavaToolchainsDiscovery::class),
                // TODO can't use CachingJavaHomeProbe here, don't know why
                newInstance(JavaHomeProbe::class)
            ).availableJavaHomes.map { home ->
                TestToolchain(home.dir, home.version, home.isJdk)
            }
        }
}


fun assumeCurrentToolchain(version: JavaVersion) {
    assumeThat(
        currentToolchain.version,
        equalTo(version)
    )
}


fun assumeAvailableToolchains(vararg versions: JavaVersion) {
    assumeThat(
        availableToolchains.map { it.version },
        hasItems(*versions)
    )
}
