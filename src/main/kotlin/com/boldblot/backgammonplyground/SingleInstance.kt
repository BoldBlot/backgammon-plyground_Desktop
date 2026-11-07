package com.boldblot.backgammonplyground

import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * One running app per data folder (two would fight over the database). The first copy holds a lock file and listens on
 * a loopback port (written to a file); a second copy sends its file arguments to the first and exits.
 */
object SingleInstance {
    private val lockFile get() = DataDirs.file("instance.lock")
    private val portFile get() = DataDirs.file("instance.port")
    private var channelHolder: RandomAccessFile? = null
    private var server: ServerSocket? = null

    /** True if this copy is the first and now owns the lock; [onFiles] gets paths sent by later copies (empty: just raise the window). */
    fun acquire(onFiles: (List<String>) -> Unit): Boolean {
        val raf = RandomAccessFile(lockFile, "rw")
        val lock = runCatching { raf.channel.tryLock() }.getOrNull()
        if (lock == null) { raf.close(); return false }
        channelHolder = raf // kept open for the life of the process, which keeps the lock
        val s = ServerSocket(0, 8, InetAddress.getLoopbackAddress())
        server = s
        portFile.writeText(s.localPort.toString())
        thread(name = "single-instance", isDaemon = true) {
            while (!s.isClosed) {
                val c = runCatching { s.accept() }.getOrNull() ?: break
                runCatching { c.use { it.getInputStream().bufferedReader().readLines().let(onFiles) } }
            }
        }
        return true
    }

    /** Sends [paths] to the first copy. False if it could not be reached. */
    fun send(paths: List<String>): Boolean = runCatching {
        val port = portFile.readText().trim().toInt()
        Socket(InetAddress.getLoopbackAddress(), port).use { sock ->
            sock.getOutputStream().bufferedWriter().use { w -> paths.forEach { w.write(it); w.newLine() } }
        }
        true
    }.getOrDefault(false)

    fun release() {
        runCatching { server?.close() }
        runCatching { portFile.delete() }
    }
}
