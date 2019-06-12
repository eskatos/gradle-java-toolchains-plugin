package net.grdev.java.toolchains.impl.homes

import org.gradle.api.JavaVersion
import org.gradle.cache.CacheBuilder
import org.gradle.cache.CacheRepository
import org.gradle.cache.FileLockManager
import org.gradle.cache.PersistentCache
import org.gradle.cache.PersistentIndexedCache
import org.gradle.cache.PersistentIndexedCacheParameters
import org.gradle.cache.internal.filelock.LockOptionsBuilder.mode
import org.gradle.internal.Factory
import org.gradle.internal.hash.HashCode
import org.gradle.internal.hash.Hashing
import org.gradle.internal.serialize.AbstractSerializer
import org.gradle.internal.serialize.Decoder
import org.gradle.internal.serialize.Encoder
import org.gradle.internal.serialize.HashCodeSerializer
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis


private
const val probeCacheVersion = "1"


private
const val probeCacheName = "java-toolchains-$probeCacheVersion"


internal
open class CachingJavaHomeProbe @Inject constructor(
    execActionFactory: ExecActionFactory,
    cacheRepository: CacheRepository
) : JavaHomeProbe(execActionFactory) {

    private
    val cache: PersistentCache =
        cacheRepository.cache(probeCacheName)
            .withDisplayName("Java Installation Probe Cache")
            .withCrossVersionCache(CacheBuilder.LockTarget.DefaultTarget)
            .withLockOptions(mode(FileLockManager.LockMode.None)) // lock on get
            .withProperties(mapOf("version" to probeCacheVersion))
            .open()

    private
    val store: PersistentIndexedCache<HashCode, JavaHome> =
        cache.createCache(PersistentIndexedCacheParameters.of(probeCacheName, HashCodeSerializer(), JavaHomeSerializer))

    override fun probe(javaHome: File): JavaHome {
        lateinit var model: JavaHome
        val ms = measureTimeMillis {
            model = cacheKeyFor(javaHome).let { cacheKey ->
                cache.useCache(Factory {
                    store.get(cacheKey) {
                        super.probe(javaHome)
                    }
                })
            }
        }
        logger.info("Fetched Java home '{}' from cache, took {}ms", model.description, ms)
        return model
    }

    private
    fun cacheKeyFor(javaHome: File): HashCode =
        Hashing.newHasher().apply {
            putString(javaHome.canonicalPath)
            val javaExe = javaHome.binSlashJava()
            if (javaExe.isFile) {
                putBytes(javaExe.readBytes())
            }
        }.hash()
}


private
object JavaHomeSerializer : AbstractSerializer<JavaHome>() {

    override fun write(encoder: Encoder, value: JavaHome) = encoder.run {
        when (value) {
            is JavaHome.Invalid -> {
                writeBoolean(false)
                writeString(value.dir.absolutePath)
                writeString(value.description)
            }
            is JavaHome.Valid -> {
                writeBoolean(true)
                writeString(value.dir.absolutePath)
                writeBoolean(value.isJdk)
                writeNullableString(value.version.toString())
                writeString(value.description)
            }
        }
    }

    override fun read(decoder: Decoder): JavaHome = decoder.run {
        if (readBoolean()) {
            JavaHome.Valid(
                File(readString()),
                readBoolean(),
                JavaVersion.toVersion(readString()),
                readString()
            )
        } else {
            JavaHome.Invalid(
                File(readString()),
                readString()
            )
        }
    }
}
