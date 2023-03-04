package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

internal inline fun <reified T : Task> TaskContainer.register(name: String, noinline configureAction: T.() -> Unit) {
    register(name, T::class.java) { task ->
        task.configureAction()
    }
}
