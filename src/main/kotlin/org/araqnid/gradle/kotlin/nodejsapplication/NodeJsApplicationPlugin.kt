package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeTask
import org.gradle.api.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

private const val INSTALL_NCC = "installNCC"

private const val PACKAGE_WITH_NCC = "packageNodeJsDistributableWithNCC"

private const val PACKAGE_EXPLODED = "packageNodeJsDistributableExploded"

// hax to avoid depending on Kotlin plugin directly
@Suppress("UNCHECKED_CAST")
private val Task.moduleName: Provider<String> /* should be a KotlinCompile2JsTask */
    get() = javaClass.getMethod("getModuleName").invoke(this)!! as Provider<String>

private val Project.moduleNameProvider
    get() = tasks.named("compileProductionExecutableKotlinJs").flatMap { it.moduleName }

@Suppress("unused")
class NodeJsApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("base")
        target.pluginManager.apply("com.github.node-gradle.node")

        target.extensions.create("nodeJsApplication", NodeJsApplicationExtension::class.java)

        target.tasks.register<Task>(PACKAGE_EXPLODED) {
            group = "package"
            description = "Package app using a node_modules directory"
            dependsOn("productionExecutableCompileSync")
            val distDir = project.buildDir.resolve(name)
            val nodeModulesDir = project.jsBuildOutput.resolve("node_modules")
            inputs.dir(nodeModulesDir)
            val nodeJsApplicationExtension = project.extensions.getByType(NodeJsApplicationExtension::class.java)
            val moduleName = nodeJsApplicationExtension.moduleName.convention(project.moduleNameProvider)
            inputs.property("moduleName", moduleName)
            inputs.property("sourceMap", nodeJsApplicationExtension.sourceMap)
            outputs.dir(distDir)

            doLast { task ->
                task.project.copy { copySpec ->
                    copySpec.from(nodeModulesDir)
                    copySpec.into(distDir.resolve("node_modules"))
                }
                distDir.resolve("index.js").printWriter().use { pw ->
                    if (nodeJsApplicationExtension.sourceMap.get()) {
                        pw.println("require('source-map-support').install()")
                    }
                    pw.println("require('${moduleName.get()}')")
                }
            }
        }


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

            val nodeJsApplicationExtension = project.extensions.getByType(NodeJsApplicationExtension::class.java)
            val nodeExtension = project.extensions.getByType(NodeExtension::class.java)
            val toolDir = project.buildDir.resolve(INSTALL_NCC)
            val distDir = project.buildDir.resolve(name)
            val moduleName = nodeJsApplicationExtension.moduleName.convention(project.moduleNameProvider)

            inputs.dir(project.jsBuildOutput.resolve("node_modules"))
            inputs.property("nodeVersion", nodeExtension.version.convention(""))
            inputs.property("moduleName", moduleName)
            inputs.property("minify", nodeJsApplicationExtension.minify)
            inputs.property("v8cache", nodeJsApplicationExtension.v8cache)
            inputs.property("target", nodeJsApplicationExtension.target.convention(""))
            inputs.property("sourceMap", nodeJsApplicationExtension.sourceMap)
            inputs.property("externalModules", nodeJsApplicationExtension.externalModules)
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
            if (nodeJsApplicationExtension.minify.get()) {
                args.add("-m")
                args.add("--license")
                args.add("LICENSE.txt")
            }
            if (nodeJsApplicationExtension.v8cache.get()) {
                args.add("--v8-cache")
            }
            if (nodeJsApplicationExtension.target.isPresent) {
                args.add("--target")
                args.add(nodeJsApplicationExtension.target.get())
            }
            if (nodeJsApplicationExtension.sourceMap.get()) {
                args.add("-s")
            }
            for (module in nodeJsApplicationExtension.externalModules.get()) {
                args.add("-e")
                args.add(module)
            }
            doLast {
                if (nodeJsApplicationExtension.v8cache.get()) {
                    for (file in project.fileTree(distDir)) {
                        logger.info("set permissions of $file")
                        Files.setPosixFilePermissions(
                            file.toPath(), setOf(
                                PosixFilePermission.OWNER_READ,
                                PosixFilePermission.GROUP_READ,
                                PosixFilePermission.OTHERS_READ,
                            )
                        )
                    }
                }
                val nodeVersion = nodeExtension.version.orNull
                if (nodeVersion != null) {
                    logger.info("Used Node version $nodeVersion to run NCC")
                    distDir.resolve(".nvmrc").writeText(nodeVersion)
                }
            }
        }

        target.tasks.register<Zip>("nodejsDistributable") {
            group = "package"
            description = "Produce app distributable archive"

            destinationDirectory.set(project.buildDir)
            archiveAppendix.set("nodejs")
            includeEmptyDirs = false

            val nodeJsApplicationExtension = project.extensions.getByType(NodeJsApplicationExtension::class.java)
            from(project.tasks.named(if (nodeJsApplicationExtension.useNcc.get()) PACKAGE_WITH_NCC else PACKAGE_EXPLODED))
        }

        target.tasks.named("assemble").dependsOn("nodejsDistributable")
    }
}

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
