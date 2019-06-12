package net.grdev.java.toolchains.impl.homes

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.internal.JavaInstallationProbe
import org.gradle.jvm.toolchain.internal.LocalJavaInstallation
import org.gradle.process.internal.ExecActionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlin.system.measureTimeMillis


internal
open class JavaHomeProbe @Inject constructor(
    execActionFactory: ExecActionFactory
) {

    private
    val gradleProbe =
        JavaInstallationProbe(execActionFactory)

    open fun probe(javaHome: File): JavaHome =
        if (javaHome.isDirectory) {
            gradleProbe.probeJavaHome(javaHome)
        } else {
            JavaHome.Invalid(javaHome, "Directory '$javaHome' doesn't exist")
        }
}

internal
val logger: Logger = LoggerFactory.getLogger(JavaHomeProbe::class.java)


private
data class ProbedModel(
    val javaHomeFile: File,
    val isJdk: Boolean,
    val error: String?
) : LocalJavaInstallation {

    private val randomName = "probed_${Random.nextInt()}"
    override fun getName() = randomName

    override fun getJavaHome() = javaHomeFile
    override fun setJavaHome(home: File) = Unit

    private var localVersion: JavaVersion? = null
    private var localDisplayName: String? = null

    override fun getJavaVersion() = localVersion
    override fun getDisplayName() = localDisplayName

    override fun setJavaVersion(version: JavaVersion) {
        localVersion = version
    }

    override fun setDisplayName(displayName: String) {
        localDisplayName = displayName
    }
}


private
fun JavaInstallationProbe.probeJavaHome(javaHome: File): JavaHome {

    lateinit var probedModel: ProbedModel
    val ms = measureTimeMillis {

        val probeResult = checkJdk(javaHome)

        val jdk: Boolean
        val error: String?
        when (probeResult.installType) {
            JavaInstallationProbe.InstallType.IS_JDK -> {
                jdk = true
                error = null
            }
            JavaInstallationProbe.InstallType.IS_JRE -> {
                jdk = false
                error = null
            }
            JavaInstallationProbe.InstallType.INVALID_JDK -> {
                jdk = false
                error = probeResult.error
            }
            JavaInstallationProbe.InstallType.NO_SUCH_DIRECTORY -> {
                jdk = false
                error = probeResult.error
            }
            else -> {
                jdk = false
                error = null
            }
        }


        probedModel = ProbedModel(javaHome, jdk, error)

        if (error == null) {
            probeResult.configure(probedModel)
        }
    }

    logger.info("Probed '{}', Java '{}', took {}ms", javaHome, probedModel.displayName, ms)

    return if (probedModel.error != null) {
        JavaHome.Invalid(javaHome, probedModel.error!!)
    } else {
        JavaHome.Valid(
            javaHome,
            probedModel.isJdk,
            probedModel.javaVersion!!,
            probedModel.displayName!!
        )
    }
}
