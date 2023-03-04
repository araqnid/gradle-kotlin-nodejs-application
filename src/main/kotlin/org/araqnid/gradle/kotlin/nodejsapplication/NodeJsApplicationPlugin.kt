package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.task.NodeTask
import org.gradle.api.*
import org.gradle.api.tasks.bundling.Zip
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

private const val INSTALL_NCC = "installNCC"

private const val PACKAGE_WITH_NCC = "packageNodeJsDistributableWithNCC"

private const val ARCHIVE = "nodejsDistributable"

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

            val toolDir = project.layout.buildDirectory.dir(INSTALL_NCC)
            val distDir = project.layout.buildDirectory.dir(name)
            val moduleNameProvider = project.nodeJsApplicationExtension.moduleName.usingDefaultFrom(project)
            val operations = project.injected<InjectedOperations>()

            inputs.dir(project.jsBuildOutput.map { it.dir("node_modules") })
            inputs.property("nodeVersion", project.nodeExtension.versionIfDownloaded)
            inputs.property("moduleName", moduleNameProvider)
            inputs.property("minify", project.nodeJsApplicationExtension.minify)
            inputs.property("v8cache", project.nodeJsApplicationExtension.v8cache)
            inputs.property("target", project.nodeJsApplicationExtension.target)
            inputs.property("sourceMap", project.nodeJsApplicationExtension.sourceMap)
            inputs.property("externalModules", project.nodeJsApplicationExtension.externalModules)
            outputs.dir(distDir)

            doFirst {
                operations.delete(distDir)
            }
            script.set(toolDir.map { it.file("node_modules/@vercel/ncc/dist/ncc/cli.js") })
            args.add("build")
            args.add(moduleNameProvider.zip(project.jsBuildOutput) { moduleName, jsBuildOutput ->
                jsBuildOutput.file("node_modules/$moduleName/kotlin/$moduleName.js").toString()
            })
            args.add("-o")
            args.add(distDir.map { it.asFile.toString() })
            args.addFrom(project.nodeJsApplicationExtension.minify) {
                if (it) {
                    yield("-m")
                    yield("--license")
                    yield("LICENSE.txt")
                }
            }
            args.addFrom(project.nodeJsApplicationExtension.target) {
                if (it.isNotBlank()) {
                    yield("--target")
                    yield(it)
                }
            }
            args.addFrom(project.nodeJsApplicationExtension.sourceMap) {
                if (it) yield("-s")
            }
            args.addFrom(project.nodeJsApplicationExtension.externalModules) { modules ->
                for (module in modules) {
                    yield("-e")
                    yield(module)
                }
            }
            args.addFrom(project.nodeJsApplicationExtension.v8cache) {
                if (it) yield("--v8-cache")
            }

            val nodeVersion = project.nodeExtension.versionIfDownloaded
            val useV8Cache = project.nodeJsApplicationExtension.v8cache

            doLast {
                if (useV8Cache.get()) {
                    for (file in operations.fileTree(distDir)) {
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
                if (nodeVersion.get().isNotEmpty()) {
                    logger.info("Used Node version $nodeVersion to run NCC")
                    distDir.get().file(".nvmrc").asFile.writeText(nodeVersion.get())
                }
            }
        }

        target.tasks.register<Zip>(ARCHIVE) {
            group = "package"
            description = "Produce app distributable archive"

            destinationDirectory.set(project.layout.buildDirectory)
            archiveAppendix.set("nodejs")
            includeEmptyDirs = false

            from(project.tasks.named(PACKAGE_WITH_NCC))
        }

        target.tasks.named("assemble").configure {
            it.dependsOn(ARCHIVE)
        }
    }
}

private val Project.nodeJsApplicationExtension: NodeJsApplicationExtension
    get() = extensions.getByType(NodeJsApplicationExtension::class.java)
