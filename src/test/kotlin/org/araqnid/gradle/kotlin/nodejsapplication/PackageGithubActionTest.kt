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

    @Test
    fun `produces runnable NCC bundle`() {
        testProjectDir.path.resolve("build.gradle.kts").writeText(
            """
                plugins {
                  kotlin("js") version "1.8.10"
                  id("org.araqnid.kotlin-github-action")
                }
                
                kotlin {
                  js(IR) {
                    nodejs()
                    useCommonJs()
                    binaries.executable()
                  }
                }
                
                repositories {
                  mavenCentral()
                }
                
                dependencies {
                  implementation(kotlin("stdlib-js"))
                }
                
                actionPackaging {
                }
            """.trimIndent()
        )

        testProjectDir.path.resolve("src/main/kotlin/Example.kt")
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
}