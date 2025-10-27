package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeJsApplicationPluginTest {
    @get:Rule
    val testProjectDir = TestProjectDirectory()

    private fun setupTrivialProject() {
        testProjectDir.path.resolve("build.gradle.kts").writeText(
            """
                plugins {
                  kotlin("multiplatform") version "2.2.21"
                  id("org.araqnid.kotlin-nodejs-application")
                }
                
                kotlin {
                  js {
                    nodejs()
                    useCommonJs()
                    binaries.executable()
                  }
                }
                
                repositories {
                  mavenCentral()
                }
                
                dependencies {
                  "jsMainImplementation"(kotlin("stdlib-js"))
                }
                
                nodeJsApplication {
                }
            """.trimIndent()
        )

        testProjectDir.path.resolve("src/jsMain/kotlin/Example.kt")
            .apply {
                parent.createDirectories()
            }
            .writeText(
                """
            fun main() {
                println("hello world")
            }
        """.trimIndent()
            )
    }

    @Test
    fun `produces runnable NCC bundle`() {
        setupTrivialProject()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.path.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        assertEquals(
            result.task(":packageNodeJsDistributableWithNCC")?.outcome,
            TaskOutcome.SUCCESS,
            """packageNodeJsDistributableWithNCC was not successful:
                |${result.output}
            """.trimMargin()
        )

        assertTrue { Files.exists(testProjectDir.path.resolve("build").resolve("packageNodeJsDistributableWithNCC")) }

        assertEquals(
            runCommand(
                "node",
                testProjectDir.path.resolve("build").resolve("packageNodeJsDistributableWithNCC").toString()
            ), "hello world\n"
        )
    }

    @Test
    fun `trivial project can be built with configuration cache`() {
        setupTrivialProject()

        GradleRunner.create()
            .withProjectDir(testProjectDir.path.toFile())
            .withArguments("--configuration-cache", "assemble")
            .withPluginClasspath()
            .build()
    }

}