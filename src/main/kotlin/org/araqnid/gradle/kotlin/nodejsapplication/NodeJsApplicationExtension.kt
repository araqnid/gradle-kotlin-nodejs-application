package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

@Suppress("LeakingThis")
abstract class NodeJsApplicationExtension {
    abstract val nccVersion: Property<String>
    abstract val minify: Property<Boolean>
    abstract val v8cache: Property<Boolean>
    abstract val target: Property<String>
    abstract val sourceMap: Property<Boolean>
    abstract val moduleName: Property<String>
    abstract val externalModules: SetProperty<String>

    init {
        nccVersion.convention("latest")
        minify.convention(true)
        v8cache.convention(false)
        sourceMap.convention(false)
    }
}
