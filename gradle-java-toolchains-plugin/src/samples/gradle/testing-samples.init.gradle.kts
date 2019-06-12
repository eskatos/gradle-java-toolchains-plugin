// Gradle initialization script for testing samples
// against the under development plugin

gradle.settingsEvaluated {
    pluginManagement {
        repositories {
            // TODO local repository
            maven(url = uri(file("build/repository")))
            gradlePluginPortal()
        }
    }
}
