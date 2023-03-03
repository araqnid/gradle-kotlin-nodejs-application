package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.task.NodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val INSTALL_NCC = "installNCC"

private const val PACKAGE_WITH_NCC = "packageActionWithNCC"

@Suppress("unused")
class PackageGithubActionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("base")
        target.pluginManager.apply("com.github.node-gradle.node")

        target.extensions.create("actionPackaging", PackageGithubActionExtension::class.java)

        target.registerInstallNCCTask(INSTALL_NCC) { actionPackagingExtension.nccVersion }

        target.tasks.register<NodeTask>(PACKAGE_WITH_NCC) {
            group = "package"
            description = "Package app as a single file using NCC"

            dependsOn(INSTALL_NCC, "productionExecutableCompileSync")

            val nodeExtension = project.extensions.getByType(NodeExtension::class.java)
            val toolDir = project.layout.buildDirectory.dir(INSTALL_NCC)
            val distDir = project.layout.projectDirectory.dir("dist")
            val moduleNameProvider = project.actionPackagingExtension.moduleName.usingDefaultFrom(project)

            inputs.dir(project.jsBuildOutput.map { it.dir("node_modules") })
            inputs.property("nodeVersion", nodeExtension.version.convention(""))
            inputs.property("moduleName", moduleNameProvider)
            inputs.property("minify", project.actionPackagingExtension.minify)
            inputs.property("target", project.actionPackagingExtension.target.convention(""))
            inputs.property("sourceMap", project.actionPackagingExtension.sourceMap)
            inputs.property("externalModules", project.actionPackagingExtension.externalModules)
            outputs.dir(distDir)

            doFirst {
                project.delete(distDir)
            }
            script.set(toolDir.map { it.file("node_modules/@vercel/ncc/dist/ncc/cli.js") })
            args.add("build")
            args.add(moduleNameProvider.zip(project.jsBuildOutput) { moduleName, jsBuildOutput ->
                jsBuildOutput.file("node_modules/$moduleName/kotlin/$moduleName.js").toString()
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

        target.tasks.named("assemble").configure {
            it.dependsOn(PACKAGE_WITH_NCC)
        }
    }
}

private val Project.actionPackagingExtension
    get() = extensions.getByType(PackageGithubActionExtension::class.java)
