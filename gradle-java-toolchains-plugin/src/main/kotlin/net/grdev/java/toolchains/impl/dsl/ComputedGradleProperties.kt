package net.grdev.java.toolchains.impl.dsl

import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.PropertyInternal
import org.gradle.api.internal.provider.ProviderInternal
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File


internal
class ComputedProperty<T : Any>(
    private val property: Property<T>,
    private val provider: Provider<T>
) : Property<T>, PropertyInternal<T> by property as PropertyInternal<T> {

    override fun isPresent(): Boolean =
        property.isPresent

    override fun get(): T =
        provider.get()

    override fun getOrNull(): T? =
        provider.orNull

    override fun getOrElse(defaultValue: T): T =
        provider.getOrElse(defaultValue)

    override fun <S : Any?> map(transformer: Transformer<out S, in T>): ProviderInternal<S> =
        Providers.internal(provider).map(transformer)

    override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>, in T>): Provider<S> =
        provider.flatMap(transformer)

    override fun value(value: T?): Property<T> =
        property.value(value)

    override fun set(value: T?) =
        property.set(value)

    override fun set(provider: Provider<out T>) =
        property.set(provider)

    override fun convention(value: T): Property<T> =
        property.convention(value)

    override fun convention(valueProvider: Provider<out T>): Property<T> =
        property.convention(valueProvider)

    override fun finalizeValue() {
        Providers.internal(provider).withFinalValue()
    }
}


internal
class ComputedDirectoryProperty(
    private val property: DirectoryProperty,
    private val provider: Provider<Directory>
) : DirectoryProperty, PropertyInternal<Directory> by property as PropertyInternal<Directory> {

    override fun getAsFileTree(): FileTree =
        provider.get().asFileTree

    override fun getOrElse(defaultValue: Directory): Directory =
        provider.getOrElse(defaultValue)

    override fun value(value: Directory?): DirectoryProperty =
        property.value(value)

    override fun file(path: String): Provider<RegularFile> =
        provider.map { it.file(path) }

    override fun file(path: Provider<out CharSequence>): Provider<RegularFile> =
        provider.map { it.file(path.get().toString()) }

    override fun getOrNull(): Directory? =
        provider.orNull

    override fun set(dir: File?) =
        property.set(dir)

    override fun set(value: Directory?) =
        property.set(value)

    override fun set(provider: Provider<out Directory>) =
        property.set(provider)

    override fun getAsFile(): Provider<File> =
        provider.map { it.asFile }

    override fun isPresent(): Boolean =
        property.isPresent

    override fun convention(value: Directory): DirectoryProperty =
        property.convention(value)

    override fun convention(valueProvider: Provider<out Directory>): DirectoryProperty =
        property.convention(valueProvider)

    override fun <S : Any?> map(transformer: Transformer<out S, in Directory>): ProviderInternal<S> =
        Providers.internal(provider).map(transformer)

    override fun finalizeValue() {
        Providers.internal(provider).withFinalValue()
    }

    override fun get(): Directory =
        provider.get()

    override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>, in Directory>): Provider<S> =
        provider.flatMap(transformer)

    override fun dir(path: String): Provider<Directory> =
        provider.map { it.dir(path) }

    override fun dir(path: Provider<out CharSequence>): Provider<Directory> =
        provider.map { it.dir(path.get().toString()) }
}
