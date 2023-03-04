package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal fun Project.registerInstallNCCTask(name: String, accessNccVersion: Project.() -> Provider<String>) {
    tasks.register<NpmTask>(name) {
        val toolDir = project.layout.buildDirectory.dir(name)
        val nccScript = toolDir.map { it.file("node_modules/@vercel/ncc/dist/ncc/cli.js") }

        // every run of @vercel/ncc touches the v8 cache files, so we can't simply `outputs.dir(toolDir)` here
        outputs.file(nccScript)

        workingDir.fileProvider(toolDir.map { it.asFile })
        npmCommand.set(listOf("install"))
        args.add(project.accessNccVersion().map { "@vercel/ncc@$it" })
        val operations = project.injected<InjectedOperations>()

        doFirst {
            operations.delete(toolDir)
            toolDir.get().asFile.mkdirs()
        }

        doLast {
            check(nccScript.get().asFile.exists()) { "npm install did not produce a @vercel/ncc executable" }
        }
    }
}
