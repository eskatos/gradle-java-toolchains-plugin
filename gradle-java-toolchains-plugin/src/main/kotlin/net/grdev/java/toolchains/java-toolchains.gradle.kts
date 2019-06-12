package net.grdev

import net.grdev.java.toolchains.dsl.*
import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchainsContainer
import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchain
import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchainsDiscovery
import net.grdev.java.toolchains.impl.homes.*
import net.grdev.java.toolchains.impl.topMostParentProjectWithPlugin
import net.grdev.java.toolchains.tasks.*
import org.gradle.internal.jvm.Jvm

val pluginId = "net.grdev.java-toolchains"

val topMostProjectWithBasePlugin = topMostParentProjectWithPlugin(pluginId)

if (topMostProjectWithBasePlugin != null) {

    extensions.add(
        JavaToolchainsContainer::class,
        JavaToolchainsContainer.defaultExtensionName,
        topMostProjectWithBasePlugin.the()
    )

} else {

    if (gradle.startParameter.isConfigureOnDemand) {
        logger.warn("Plugin '$pluginId' doesn't support Configure on Demand, please disable it")
    }


    val discovery = extensions.create(
        JavaToolchainsDiscovery::class,
        "javaToolchainsDiscovery",
        DefaultJavaToolchainsDiscovery::class
    )

    val probe = objects.newInstance(CachingJavaHomeProbe::class)

    val homes = objects.newInstance(
        JavaHomes::class,
        discovery, probe,
        objects
    )

    val toolchains = extensions.create(
        JavaToolchainsContainer::class,
        JavaToolchainsContainer.defaultExtensionName,
        DefaultJavaToolchainsContainer::class,
        objects.domainObjectContainer(JavaToolchain::class, NamedDomainObjectFactory { name ->
            objects.newInstance(
                DefaultJavaToolchain::class, name,
                homes,
                objects, providers, layout
            )
        })
    )


    toolchains.register("gradle") {
        val jvm = Jvm.current()
        version.set(jvm.javaVersion)
        jdk.set(jvm.javacExecutable != null)
        javaHome.set(jvm.javaHome)
    }

    System.getenv("JAVA_HOME")?.let { path ->
        toolchains.register("JAVA_HOME") {
            javaHome.set(layout.projectDirectory.dir(path))
        }
    }


    tasks {
        register<JavaToolchainsReport>(
            JavaToolchainsReport.defaultTaskName,
            toolchains,
            homes
        )
        register<ValidateJavaToolchains>(ValidateJavaToolchains.defaultTaskName) {
            javaToolchains.convention(toolchains)
        }
    }
}
