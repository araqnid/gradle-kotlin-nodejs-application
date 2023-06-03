package org.araqnid.gradle.kotlin.nodejsapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun runCommand(vararg args: String): String = runBlocking {
    @Suppress("BlockingMethodInNonBlockingContext")
    val process = Runtime.getRuntime().exec(args)

    launch(Dispatchers.IO) {
        process.outputStream.close()
    }

    val stdout = async(Dispatchers.IO) {
        process.inputStream.reader().use { r ->
            r.readText()
        }
    }

    process.errorStream.bufferedReader().useLines { lines ->
        for (line in lines) {
            println("node: stderr: $line")
        }
    }

    process.onExit().await()
    if (process.exitValue() != 0)
        error("node process exited with ${process.exitValue()}")

    stdout.await()
}
