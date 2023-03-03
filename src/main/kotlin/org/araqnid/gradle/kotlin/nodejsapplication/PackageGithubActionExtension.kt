package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

@Suppress("LeakingThis")
abstract class PackageGithubActionExtension {
    abstract val nccVersion: Property<String>
    abstract val minify: Property<Boolean>
    abstract val target: Property<String>
    abstract val sourceMap: Property<Boolean>
    abstract val moduleName: Property<String>
    abstract val externalModules: SetProperty<String>

    init {
        minify.convention(false)
        sourceMap.convention(false)
        nccVersion.convention("latest")
    }
}
