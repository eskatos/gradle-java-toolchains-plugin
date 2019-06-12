package net.grdev.java.toolchains.impl.dsl

import net.grdev.java.toolchains.dsl.JavaToolchain
import net.grdev.java.toolchains.dsl.JavaToolchainsContainer
import org.gradle.api.NamedDomainObjectContainer
import javax.inject.Inject

internal
abstract class DefaultJavaToolchainsContainer @Inject constructor(
    container: NamedDomainObjectContainer<JavaToolchain>
) : JavaToolchainsContainer, NamedDomainObjectContainer<JavaToolchain> by container
