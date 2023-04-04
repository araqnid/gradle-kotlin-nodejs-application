package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

@Suppress("LeakingThis")
abstract class PackageGithubActionExtension : NodeJsPackagingExtension {
    abstract override val nccVersion: Property<String>
    abstract override val minify: Property<Boolean>
    abstract override val target: Property<String>
    abstract override val sourceMap: Property<Boolean>
    abstract override val moduleName: Property<String>
    abstract override val externalModules: SetProperty<String>

    init {
        minify.convention(false)
        sourceMap.convention(false)
        nccVersion.convention("latest")
        target.convention("")
    }
}
