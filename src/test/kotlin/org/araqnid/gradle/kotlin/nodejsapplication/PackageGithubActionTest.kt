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

class PackageGithubActionTest {
    @get:Rule
    val testProjectDir = TestProjectDirectory()

    private fun setupTrivialProject() {
        testProjectDir.path.resolve("build.gradle.kts").writeText(
            """
                plugins {
                  kotlin("multiplatform") version "2.2.21"
                  id("org.araqnid.kotlin-github-action")
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
                
                actionPackaging {
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
            result.task(":packageActionWithNCC")?.outcome,
            TaskOutcome.SUCCESS,
            """packageActionWithNCC was not successful:
                |${result.output}
            """.trimMargin()
        )

        assertTrue { Files.exists(testProjectDir.path.resolve("dist")) }

        assertEquals(
            runCommand(
                "node",
                testProjectDir.path.resolve("dist").toString()
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