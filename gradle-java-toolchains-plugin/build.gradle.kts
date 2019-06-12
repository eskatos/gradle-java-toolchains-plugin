plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish")
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
}


tasks {
    validateTaskProperties {
        failOnWarning = true
        enableStricterValidation = true
    }
}


pluginBundle {

    val namespace = "net.grdev"
    val githubUserName = "eskatos"
    val githubRepoName = "gradle-java-toolchains-plugin"
    val baseTags = listOf("java", "cross-compilation", "jvm", "install")

    website = "https://$githubUserName.github.com/$githubRepoName"
    vcsUrl = "https://github.com/$githubUserName/$githubRepoName"

    (plugins) {
        "$namespace.java-toolchains" {
            displayName = ""
            description = ""
            tags = baseTags + listOf()
        }
    }
}

// Local repository

val repositoryDir = file("$buildDir/repository")

val publishToBuildRepository by tasks.registering {
    dependsOn("publishPluginMavenPublicationToTestRepository")
}
tasks.test {
    dependsOn(publishToBuildRepository)
    inputs.dir(repositoryDir)
}

afterEvaluate {

    publishing {
        repositories {
            maven {
                name = "test"
                url = uri(repositoryDir)
            }
        }
    }

    gradlePlugin {
        plugins.all {
            val plugin = this
            publishToBuildRepository {
                dependsOn("publish${plugin.name.capitalize()}PluginMarkerMavenPublicationToTestRepository")
            }
        }
    }
}
