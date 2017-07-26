package de.georghenkel.webhooklistener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.kotlin.*
import java.io.File
import java.util.concurrent.TimeUnit

fun String.runCommand(workingDir: File) {
    val parts = this.split("\\s".toRegex())
    ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
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

    ignite().port(8080).post("/webhook") {
        "git pull".runCommand(workingDir)
        "jbake -b . $target".runCommand(workingDir)
    }
}