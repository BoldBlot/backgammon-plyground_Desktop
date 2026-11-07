package com.boldblot.backgammonplyground

import java.io.File

/** Where the desktop app keeps its files: `$XDG_DATA_HOME/BackgammonPlyGround`, else `~/.local/share/BackgammonPlyGround`. */
object DataDirs {
    val data: File by lazy {
        val base = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() }?.let(::File)
            ?: File(System.getProperty("user.home"), ".local/share")
        File(base, "BackgammonPlyGround").also { it.mkdirs() }
    }

    fun file(name: String): File = File(data, name)
    fun dir(name: String): File = File(data, name).also { it.mkdirs() }
}
