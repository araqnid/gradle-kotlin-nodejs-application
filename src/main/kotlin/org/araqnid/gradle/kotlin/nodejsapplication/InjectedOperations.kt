package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Project
import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

internal inline fun <reified T> Project.injected(): T {
    return objects.newInstance(T::class.java)
}

internal interface InjectedOperations {
    @get:Inject
    val fs: FileSystemOperations
}

internal fun InjectedOperations.delete(target: Any) {
    fs.delete {
        it.delete(target)
    }
}
