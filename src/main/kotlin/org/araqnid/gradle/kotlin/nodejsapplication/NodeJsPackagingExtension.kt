package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface NodeJsPackagingExtension {
    /**
     * Version of NCC to use, defaults to "latest"
     */
    val nccVersion: Property<String>

    /**
     * Whether to minify JS code bundle
     *
     * Corresponds to `ncc --minify`
     */
    val minify: Property<Boolean>

    /**
     * ECMAScript target for output
     *
     * Corresponds to `ncc --target`
     */
    val target: Property<String>

    /**
     * Whether to produce JS source map
     *
     * Corresponds to `ncc --source-map`
     */
    val sourceMap: Property<Boolean>

    /**
     * Kotlin module name.
     *
     * Required to find the JS file: by default, gleaned from the Kotlin plugin configuration.
     */
    val moduleName: Property<String>

    /**
     * External JS module names to exclude from bundle.
     *
     * Corresponds to `ncc --external`
     */
    val externalModules: SetProperty<String>
}
