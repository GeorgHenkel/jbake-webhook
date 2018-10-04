package de.georghenkel.webhooklistener

import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.concurrent.thread

fun String.runCommand(workingDir: File, logger: Logger) {
    val parts = this.split("\\s".toRegex())
    logger.info("Workingdir: " + workingDir + ", Parts: " + parts.joinToString())
    val process = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .start()

    thread(start = true) {
        process.inputStream.bufferedReader().useLines { it.forEach { logger.info(it) } }
    }

    thread(start = true) {
        process.errorStream.bufferedReader().useLines { it.forEach { logger.info(it) } }
    }

    process.waitFor()
}

fun main(args: Array<String>) {
    val log: Logger = LoggerFactory.getLogger("WebhookListener")

    val workingDirectory = System.getProperty("workingdir")
    if (workingDirectory == null) {
        log.error("Please specify -Dworkingdir")
        return
    }

    val target = System.getProperty("target")
    if (target == null) {
        log.error("Please specify -Dtarget")
        return
    }

    val workingDir = File(workingDirectory)
    if (!workingDir.exists() || !workingDir.isDirectory || !workingDir.canWrite()) {
        log.error("Working dir $workingDirectory does not exist or is not writable")
    }

    val targetDir = File(target)
    if (!targetDir.exists() || !targetDir.isDirectory || !targetDir.canWrite()) {
        log.error("Target dir $targetDir does not exist or is not writable")
    }

    val app = Javalin
            .create()
            .defaultContentType("text/plain")
            .start(8080)

    app.get("/alive") { ctx ->
        val dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        ctx.result(dateTime)
    }

    app.post("/webhook") { ctx ->
        runCommands(workingDir, target, log)
        ctx.result("SUCCESS")
    }
}

private fun runCommands(workingDir: File, target: String, log: Logger) {
    Executors.newSingleThreadExecutor().execute({
        "git pull origin master".runCommand(workingDir, log)
        "jbake -b . $target".runCommand(workingDir, log)
    })
}