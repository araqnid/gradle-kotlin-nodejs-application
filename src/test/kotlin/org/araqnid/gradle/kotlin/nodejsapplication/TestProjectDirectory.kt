package org.araqnid.gradle.kotlin.nodejsapplication

import org.junit.rules.ExternalResource
import java.nio.file.Files
import java.nio.file.Path

class TestProjectDirectory : ExternalResource() {
    private lateinit var root: Path

    val path: Path
        get() = root

    override fun before() {
        root = Files.createTempDirectory("testProjectDir")
    }

    override fun after() {
        Files.walk(root).use { pathStream ->
            for (path in pathStream.toList().asReversed()) {
                Files.delete(path)
            }
        }
    }
}