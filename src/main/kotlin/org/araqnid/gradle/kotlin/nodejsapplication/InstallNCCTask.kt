package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Project

internal const val NCC_SCRIPT_PATH_FROM_TOOL_DIR = "node_modules/.bin/ncc"

internal fun Project.registerInstallNCCTask(name: String, extension: NodeJsPackagingExtension) {
    tasks.register<NpmTask>(name) {
        val toolDir = project.layout.buildDirectory.dir(name)
        val nccScript = toolDir.map { it.file(NCC_SCRIPT_PATH_FROM_TOOL_DIR) }

        inputs.property("nccVersion", extension.nccVersion)

        // every run of @vercel/ncc touches the v8 cache files, so we can't simply `outputs.dir(toolDir)` here
        outputs.file(nccScript)

        workingDir.fileProvider(toolDir.map { it.asFile })
        npmCommand.set(listOf("install"))
        args.add("--prefix")
        args.add(toolDir.map { it.asFile.toString() })
        args.add(extension.nccVersion.map { "@vercel/ncc@$it" })
        val operations = project.injected<InjectedOperations>()

        doFirst {
            operations.delete(toolDir)
            toolDir.get().asFile.mkdirs()
        }

        doLast {
            check(nccScript.get().asFile.exists()) { "npm install did not produce a @vercel/ncc executable" }
            logger.info("Installed NCC at ${nccScript.get()}")
        }
    }
}
