package com.eginez.kopass

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit


data class ProcessResult (val output: InputStream, val error: InputStream, val exitCode: Int)

fun refresh(pathToRoot: String) : Boolean {
    val result = "git pull --rebase".runCommand(File(pathToRoot))
    return result?.exitCode == 0
}

fun decryptgpg(pathToFile: String): String? {
    val command = "/usr/local/bin/gpg2 --no-tty --decrypt $pathToFile"
    val result = command.runCommand(File(System.getProperty("user.dir")))
    if (result?.exitCode == 0) {
        return result.output.bufferedReader().readText()
    } else  if (result?.exitCode != 0) {
        val error = result?.error?.bufferedReader()?.readText()
        val msg = "Fail to execute $command pass $pathToFile with error $error"
        println(msg)
        val ex = RuntimeException(msg)
        throw ex
    }
    return null
}
fun decrypt(pathToFile: String): String? {
    val result = "/usr/local/bin/pass $pathToFile".runCommand(File(System.getProperty("user.dir")))
    if (result?.exitCode == 0) {
        return result.output.bufferedReader().readText()
    } else  if (result?.exitCode != 0) {
        val error = result?.error?.bufferedReader()?.readText()
        val msg = "Fail to execute comannd pass $pathToFile wiht error $error"
        val ex = RuntimeException(msg)
        ex.log()
    }
    return null
}


fun String.runCommand(workingDir: File): ProcessResult? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return ProcessResult(proc.inputStream, proc.errorStream, proc.exitValue())
    } catch(e: IOException) {
        e.log()
        return null
    }
}

