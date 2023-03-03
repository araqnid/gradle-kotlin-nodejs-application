package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

private const val INSTALL_NCC = "installNCC"

private const val PACKAGE_WITH_NCC = "packageActionWithNCC"

// hax to avoid depending on Kotlin plugin directly
@Suppress("UNCHECKED_CAST")
private val Task.moduleName: Provider<String> /* should be a KotlinCompile2JsTask */
    get() = javaClass.getMethod("getModuleName").invoke(this)!! as Provider<String>

private val Project.moduleNameProvider
    get() = tasks.named("compileProductionExecutableKotlinJs").flatMap { it.moduleName }

@Suppress("unused")
class PackageGithubActionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("base")
        target.pluginManager.apply("com.github.node-gradle.node")

        target.extensions.create("actionPackaging", PackageGithubActionExtension::class.java)

        target.tasks.register<NpmTask>(INSTALL_NCC) {
            val toolDir = project.buildDir.resolve(name)
            val nccScript = toolDir.resolve("node_modules/@vercel/ncc/dist/ncc/cli.js")

            // every run of @vercel/ncc touches the v8 cache files, so we can't simply `outputs.dir(toolDir)` here
            outputs.file(nccScript)

            workingDir.set(toolDir)
            npmCommand.set(listOf("install"))
            args.set(listOf("@vercel/ncc"))

            doFirst {
                project.delete(toolDir)
                toolDir.mkdirs()
            }

            doLast {
                check(nccScript.exists()) { "npm install did not produce a @vercel/ncc executable" }
            }
        }

        target.tasks.register<NodeTask>(PACKAGE_WITH_NCC) {
            group = "package"
            description = "Package app as a single file using BCC"

            dependsOn(INSTALL_NCC, "productionExecutableCompileSync")

            val nodeExtension = project.extensions.getByType(NodeExtension::class.java)
            val toolDir = project.buildDir.resolve(INSTALL_NCC)
            val distDir = project.file("dist")
            val moduleName = project.actionPackagingExtension.moduleName.convention(project.moduleNameProvider)

            inputs.dir(project.jsBuildOutput.resolve("node_modules"))
            inputs.property("nodeVersion", nodeExtension.version.convention(""))
            inputs.property("moduleName", moduleName)
            inputs.property("minify", project.actionPackagingExtension.minify)
            inputs.property("target", project.actionPackagingExtension.target.convention(""))
            inputs.property("sourceMap", project.actionPackagingExtension.sourceMap)
            inputs.property("externalModules", project.actionPackagingExtension.externalModules)
            outputs.dir(distDir)

            doFirst {
                project.delete(distDir)
            }
            script.set(toolDir.resolve("node_modules/@vercel/ncc/dist/ncc/cli.js"))
            args.add("build")
            args.add(moduleName.map {
                project.jsBuildOutput.resolve("node_modules/$it/kotlin/$it.js").toString()
            })
            args.add("-o")
            args.add(distDir.toString())
            if (project.actionPackagingExtension.minify.get()) {
                args.add("-m")
                args.add("--license")
                args.add("LICENSE.txt")
            }
            if (project.actionPackagingExtension.target.isPresent) {
                args.add("--target")
                args.add(project.actionPackagingExtension.target.get())
            }
            if (project.actionPackagingExtension.sourceMap.get()) {
                args.add("-s")
            }
            for (module in project.actionPackagingExtension.externalModules.get()) {
                args.add("-e")
                args.add(module)
            }
        }

        target.tasks.named("assemble").dependsOn(PACKAGE_WITH_NCC)
    }
}

private val Project.actionPackagingExtension
    get() = extensions.getByType(PackageGithubActionExtension::class.java)

private val Project.jsBuildOutput
    get() = rootProject.buildDir.resolve("js")

private fun TaskProvider<*>.dependsOn(otherTask: String) {
    configure {
        it.dependsOn(otherTask)
    }
}

private inline fun <reified T : Task> TaskContainer.register(name: String, noinline configureAction: T.() -> Unit) {
    register(name, T::class.java) { task ->
        task.configureAction()
    }
}
