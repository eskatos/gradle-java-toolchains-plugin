package net.grdev.java.toolchains.impl.homes

import net.grdev.java.toolchains.impl.dsl.DefaultJavaToolchainsDiscovery
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject


internal
open class JavaHomes @Inject constructor(
    private val discovery: DefaultJavaToolchainsDiscovery,
    private val probe: JavaHomeProbe,
    private val objects: ObjectFactory
) {

    val availableJavaHomes: List<JavaHome.Valid> by lazy {
        discovery.finalizeValues()
        discoverJavaHomes(discovery, objects)
            .map { javaHome(it) }
            .filterIsInstance<JavaHome.Valid>()
    }

    fun javaHome(dir: File): JavaHome =
        probe.probe(dir)
}
