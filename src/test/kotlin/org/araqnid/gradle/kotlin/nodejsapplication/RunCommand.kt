package org.araqnid.gradle.kotlin.nodejsapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun runCommand(vararg args: String): String {
    return runBlocking {
        @Suppress("BlockingMethodInNonBlockingContext")
        val process = Runtime.getRuntime().exec(args)

        val stdin = launch(Dispatchers.IO) {
            process.outputStream.close()
        }

        val stdout = async(Dispatchers.IO) {
            process.inputStream.reader(Charsets.UTF_8).use { r ->
                r.readText()
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
        stderr.join()

        process.onExit().await()
        if (process.exitValue() != 0)
            error("node process exited with ${process.exitValue()}")

        stdout.await()
    }
}
