package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal inline fun <reified T : Any> Project.injected(): T {
    return objects.newInstance(T::class.java)
}

internal interface InjectedOperations {
    @get:Inject
    val fs: FileSystemOperations

    @get:Inject
    val objects: ObjectFactory
}

internal fun InjectedOperations.delete(target: Any) {
    fs.delete {
        it.delete(target)
    }
}

internal fun InjectedOperations.fileTree(target: Any): ConfigurableFileTree {
    return objects.fileTree().apply {
        setDir(target)
    }
}