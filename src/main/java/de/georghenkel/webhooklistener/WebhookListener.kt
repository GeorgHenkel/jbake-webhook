package de.georghenkel.webhooklistener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.kotlin.*
import java.io.File
import kotlin.concurrent.thread

fun String.runCommand(workingDir: File, logger: Logger) {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .start()

    thread(start = true) {
        process.inputStream.bufferedReader().useLines {  it.forEach { logger.info(it) } }
    }

    thread(start = true) {
        process.errorStream.bufferedReader().useLines {  it.forEach { logger.info(it) } }
    }

    process.waitFor()
}

fun main(args: Array<String>) {
    val LOG: Logger = LoggerFactory.getLogger("WebhookListener")

    val workingDirectory = System.getProperty("workingdir")
    if (workingDirectory == null) {
        LOG.error("Please specify -Dworkingdir")
        return
    }

    val target = System.getProperty("target")
    if (target == null) {
        LOG.error("Please specify -Dtarget")
        return
    }

    val workingDir = File(workingDirectory)
    if (!workingDir.exists() || !workingDir.isDirectory || !workingDir.canWrite()) {
        LOG.error("Working dir $workingDirectory does not exist or is not writable")
    }

    val targetDir = File(target)
    if (!targetDir.exists() || !targetDir.isDirectory || !targetDir.canWrite()) {
        LOG.error("Target dir $targetDir does not exist or is not writable")
    }

    ignite().port(8080)

    post("/webhook", "application/json", {
        "git pull".runCommand(workingDir, LOG)
        "jbake -b . $target".runCommand(workingDir, LOG)
    })
}