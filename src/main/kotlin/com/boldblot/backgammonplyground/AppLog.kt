package com.boldblot.backgammonplyground

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A small log kept on the computer (recent game events, engine problems, crashes). It never leaves the
 * computer by itself: the player sends it with "Send error report", which opens their mail client with
 * the log saved to a file and the support address filled in.
 */
object AppLog {
    private const val MAX_BYTES = 256 * 1024
    const val SUPPORT_EMAIL = "support@boldblot.com"

    /** Told about an uncaught exception (the window shows a dialog once). */
    @Volatile var onCrash: ((Throwable) -> Unit)? = null

    private var dir: File? = null
    private val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)

    private val file get() = dir?.let { File(it, "app.log") }

    /** Starts logging into the data folder and records crashes before the app ends. */
    fun init() {
        dir = DataDirs.dir("logs")
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            error("CRASH on ${thread.name}", e)
            onCrash?.invoke(e)
            previous?.uncaughtException(thread, e)
        }
        i("App started: ${BuildConfig.VERSION_NAME}")
    }

    fun i(message: String) = write("I", message)

    fun error(message: String, e: Throwable? = null) {
        val trace = e?.let { StringWriter().also { w -> it.printStackTrace(PrintWriter(w)) }.toString() }
        write("E", if (trace == null) message else "$message\n$trace")
    }

    @Synchronized
    private fun write(level: String, message: String) {
        val f = file ?: return
        runCatching {
            // Keep the newest half when the log is full.
            if (f.length() > MAX_BYTES) {
                val keep = f.readText().takeLast(MAX_BYTES / 2)
                f.writeText("[older entries removed]\n" + keep.substringAfter('\n'))
            }
            f.appendText("${stamp.format(Date())} $level $message\n")
        }
    }

    /** The log, copied to a file in the data folder's reports folder. */
    fun saveReport(): File {
        val out = DataDirs.dir("reports")
        val name = "plyground-log-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(Date())}.txt"
        val copy = File(out, name)
        out.listFiles()?.forEach { it.delete() }
        synchronized(this) { copy.writeText(file?.takeIf { it.exists() }?.readText() ?: "(empty log)\n") }
        return copy
    }

    /**
     * Saves the log to a file and opens the mail client with the support address and the versions filled in.
     * A mailto: link can't attach files, so the message names the saved file. Returns the file.
     */
    fun sendReport(): File {
        i("Error report opened")
        val copy = saveReport()
        val body = """
            |What happened, and what did you do just before?
            |
            |
            |---
            |Backgammon PlyGround ${BuildConfig.VERSION_NAME}
            |${System.getProperty("os.name")} ${System.getProperty("os.version")}, ${System.getProperty("os.arch")}
            |Please attach the log: ${copy.path}
        """.trimMargin()
        val subject = "Backgammon PlyGround error report (${BuildConfig.VERSION_NAME})"
        runCatching {
            val uri = "mailto:$SUPPORT_EMAIL?subject=${encodeUri(subject)}&body=${encodeUri(body)}"
            java.awt.Desktop.getDesktop().browse(java.net.URI(uri))
        }.onFailure { error("Could not open the mail client", it) }
        return copy
    }
}
