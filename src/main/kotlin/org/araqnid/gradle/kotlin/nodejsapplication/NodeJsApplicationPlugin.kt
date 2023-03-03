package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.task.NodeTask
import org.gradle.api.*
import org.gradle.api.tasks.bundling.Zip
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

private const val INSTALL_NCC = "installNCC"

private const val PACKAGE_WITH_NCC = "packageNodeJsDistributableWithNCC"

@Suppress("unused")
class NodeJsApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("base")
        target.pluginManager.apply("com.github.node-gradle.node")

        target.extensions.create("nodeJsApplication", NodeJsApplicationExtension::class.java)

        target.registerInstallNCCTask(INSTALL_NCC) { project.nodeJsApplicationExtension.nccVersion }

        target.tasks.register<NodeTask>(PACKAGE_WITH_NCC) {
            group = "package"
            description = "Package app as a single file using NCC"

            dependsOn(INSTALL_NCC, "productionExecutableCompileSync")

            val nodeExtension = project.extensions.getByType(NodeExtension::class.java)
            val toolDir = project.buildDir.resolve(INSTALL_NCC)
            val distDir = project.buildDir.resolve(name)
            val moduleName = project.nodeJsApplicationExtension.moduleName.convention(project.moduleNameProvider)

            inputs.dir(project.jsBuildOutput.resolve("node_modules"))
            inputs.property("nodeVersion", nodeExtension.version.convention(""))
            inputs.property("moduleName", moduleName)
            inputs.property("minify", project.nodeJsApplicationExtension.minify)
            inputs.property("v8cache", project.nodeJsApplicationExtension.v8cache)
            inputs.property("target", project.nodeJsApplicationExtension.target.convention(""))
            inputs.property("sourceMap", project.nodeJsApplicationExtension.sourceMap)
            inputs.property("externalModules", project.nodeJsApplicationExtension.externalModules)
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
            if (project.nodeJsApplicationExtension.minify.get()) {
                args.add("-m")
                args.add("--license")
                args.add("LICENSE.txt")
            }
            if (project.nodeJsApplicationExtension.v8cache.get()) {
                args.add("--v8-cache")
            }
            if (project.nodeJsApplicationExtension.target.isPresent) {
                args.add("--target")
                args.add(project.nodeJsApplicationExtension.target.get())
            }
            if (project.nodeJsApplicationExtension.sourceMap.get()) {
                args.add("-s")
            }
            for (module in project.nodeJsApplicationExtension.externalModules.get()) {
                args.add("-e")
                args.add(module)
            }
            doLast {
                if (project.nodeJsApplicationExtension.v8cache.get()) {
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

            from(project.tasks.named(PACKAGE_WITH_NCC))
        }

        target.tasks.named("assemble").configure {
            it.dependsOn("nodejsDistributable")
        }
    }
}

private val Project.nodeJsApplicationExtension
    get() = project.extensions.getByType(NodeJsApplicationExtension::class.java)

private val Project.jsBuildOutput
    get() = rootProject.buildDir.resolve("js")
