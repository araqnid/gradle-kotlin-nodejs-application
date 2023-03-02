package org.araqnid.gradle.kotlin.nodejsapplication

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.*

class NodeJsApplicationPluginTest {
    private lateinit var testProjectDir: Path

    @BeforeTest
    fun setup() {
        testProjectDir = Files.createTempDirectory("testProjectDir")
    }

    @AfterTest
    fun cleanup() {
        Files.walk(testProjectDir).use { pathStream ->
            for (path in pathStream.toList().asReversed()) {
                Files.delete(path)
            }
        }
    }

    @Test
    fun `produces runnable NCC bundle`() {
        testProjectDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                  kotlin("js") version "1.8.10"
                  id("org.araqnid.kotlin-nodejs-application")
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
                
                nodeJsApplication {
                }
            """.trimIndent()
        )

        testProjectDir.resolve("src/main/kotlin/Example.kt")
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
            .withProjectDir(testProjectDir.toFile())
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

        assertTrue { Files.exists(testProjectDir.resolve("build").resolve("packageNodeJsDistributableWithNCC")) }

        assertEquals(
            runCommand(
                "node",
                testProjectDir.resolve("build").resolve("packageNodeJsDistributableWithNCC").toString()
            ), "hello world\n"
        )
    }

    private fun runCommand(vararg args: String): String {
        return runBlocking {
            @Suppress("BlockingMethodInNonBlockingContext")
            val process = Runtime.getRuntime().exec(args)

            val stdin = launch(Dispatchers.IO) {
                process.outputStream.close()
            }

            val stdout = async(Dispatchers.IO) {
                try {
                    process.inputStream.readAllBytes().toString(Charsets.UTF_8)
                } finally {
                    process.inputStream.close()
                }
            }

            val stderr = launch(Dispatchers.IO) {
                process.errorStream.bufferedReader().useLines { lines ->
                    for (line in lines) {
                        println("node: stderr: $line")
                    }
                }
            }

            stdin.join()
            stdout.join()
            stderr.join()

            val exitCode = process.onExit().await()
            if (exitCode.exitValue() != 0)
                error("node process exited with $exitCode")

            stdout.await()
        }
    }
}