package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.NamedDomainObjectCollection
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

internal fun Provider<String>.usingModuleNameFrom(project: Project) = orElse(project.moduleNameProvider)

@Suppress("UNCHECKED_CAST")
internal val Project.moduleKindProvider: Provider<String>
    get() {
        val kotlinMultiplatformExtension = extensions.getByName("kotlin")
        val jsDSL = kotlinMultiplatformExtension.javaClass.getMethod("js").invoke(kotlinMultiplatformExtension)
        val compilationsCollection =
            jsDSL.javaClass.getMethod("getCompilations").invoke(jsDSL) as NamedDomainObjectCollection<Any>
        val mainCompilation = compilationsCollection.getByName("main")
        val mainCompilerOptions = mainCompilation.javaClass.getMethod("getCompilerOptions").invoke(mainCompilation)
        val options =
            mainCompilerOptions.javaClass.getMethod("getOptions").invoke(mainCompilerOptions) // KotlinJsCompilerOptions
        val moduleKind = options.javaClass.getMethod("getModuleKind").invoke(options) as Provider<Any>
        return moduleKind.map { it.toString() }
    }

internal fun Provider<String>.usingModuleKindFrom(project: Project) = orElse(project.moduleKindProvider)
