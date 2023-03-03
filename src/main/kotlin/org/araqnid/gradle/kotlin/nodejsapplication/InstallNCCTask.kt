package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun Project.registerInstallNCCTask(name: String, accessNccVersion: Project.() -> Provider<String>) {
    tasks.register<NpmTask>(name) {
        val toolDir = project.buildDir.resolve(name)
        val nccScript = toolDir.resolve("node_modules/@vercel/ncc/dist/ncc/cli.js")

        // every run of @vercel/ncc touches the v8 cache files, so we can't simply `outputs.dir(toolDir)` here
        outputs.file(nccScript)

        workingDir.set(toolDir)
        npmCommand.set(listOf("install"))
        args.add(project.accessNccVersion().map { "@vercel/ncc@$it" })

        doFirst {
            project.delete(toolDir)
            toolDir.mkdirs()
        }

        doLast {
            check(nccScript.exists()) { "npm install did not produce a @vercel/ncc executable" }
        }
    }
}
