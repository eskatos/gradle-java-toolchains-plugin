plugins {
    java
    id("net.grdev.java-toolchains") version "0"
}


// Lazy configuration

val java12 by javaToolchains.registering {
    version.set(JavaVersion.VERSION_12)
}

tasks.register<JavaExec>("javaExec") {
    java12.get().configure(this)
    classpath = files(sourceSets.main.map { it.output })
    main = "org.example.Main"
    args = listOf("javaExec")
}


// Eager configuration

val eagerJava10 by javaToolchains.creating {
    version.set(JavaVersion.VERSION_1_10)
}

task<JavaExec>("eagerJavaExec") {
    eagerJava10.configure(this)
    classpath = files(sourceSets.main.map { it.output })
    main = "org.example.Main"
    args = listOf("eagerJavaExec")
}
