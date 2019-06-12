package net.grdev.java.toolchains.impl

import org.gradle.api.Project


internal
fun Project.topMostParentProjectWithPlugin(id: String): Project? {
    var topMostParentProjectWithPlugin: Project? = null
    var current: Project? = project.parent
    while (current != null) {
        if (current.plugins.hasPlugin(id)) {
            topMostParentProjectWithPlugin = current
        }
        current = current.parent
    }
    return topMostParentProjectWithPlugin
}
