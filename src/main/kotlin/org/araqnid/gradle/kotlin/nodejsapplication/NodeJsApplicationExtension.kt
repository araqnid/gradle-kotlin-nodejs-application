package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

@Suppress("LeakingThis")
abstract class NodeJsApplicationExtension : NodeJsPackagingExtension {
    abstract override val nccVersion: Property<String>
    abstract override val minify: Property<Boolean>

    /**
     * Whether to produce V8 cache file.
     *
     * Corresponds to `ncc --v8-cache`. Default is to not produce cache file.
     */
    abstract val v8cache: Property<Boolean>
    abstract override val target: Property<String>
    abstract override val sourceMap: Property<Boolean>
    abstract override val moduleName: Property<String>
    abstract override val externalModules: SetProperty<String>

    init {
        nccVersion.convention("latest")
        minify.convention(true)
        v8cache.convention(false)
        sourceMap.convention(true)
        target.convention("")
    }
}
