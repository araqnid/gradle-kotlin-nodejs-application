package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

// hax to avoid depending on Kotlin plugin directly
@Suppress("UNCHECKED_CAST")
private val Task.moduleName: Provider<String> /* should be a KotlinCompile2JsTask */
    get() = javaClass.getMethod("getModuleName").invoke(this)!! as Provider<String>

private val Project.moduleNameProvider: Provider<String>
    get() = tasks.named("compileProductionExecutableKotlinJs").flatMap { it.moduleName }

internal fun Provider<String>.usingDefaultFrom(project: Project): Provider<String> {
    return orElse(project.moduleNameProvider)
}
