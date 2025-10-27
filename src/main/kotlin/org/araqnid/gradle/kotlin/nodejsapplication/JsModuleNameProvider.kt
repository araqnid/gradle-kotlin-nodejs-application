package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Project
import org.gradle.api.provider.Provider

// hax to avoid depending on Kotlin plugin directly
@Suppress("UNCHECKED_CAST")
private val Project.moduleNameProvider: Provider<String>
    get() {
        val kotlinMultiplatformExtension = extensions.getByName("kotlin")
        val jsMethod = kotlinMultiplatformExtension.javaClass.getMethod("js")
        val jsDSL = jsMethod.invoke(kotlinMultiplatformExtension)
        val outputModuleNameGetter = jsDSL.javaClass.getMethod("getOutputModuleName")
        return outputModuleNameGetter.invoke(jsDSL) as org.gradle.api.provider.Property<String>
    }

internal fun Provider<String>.usingDefaultFrom(project: Project) = orElse(project.moduleNameProvider)
