package com.boldblot.backgammonplyground

import java.util.Properties

/** The window's size, position and maximized state, remembered between runs (a small file in the data folder). */
object WindowPrefs {
    /** The main window can't get smaller than this: room for the board and the side panel. */
    const val MIN_WIDTH = 900
    const val MIN_HEIGHT = 600

    data class Saved(val width: Int, val height: Int, val x: Int?, val y: Int?, val maximized: Boolean)

    /** [name] is a separate window's; none is the main window's. */
    private fun file(name: String?) = DataDirs.file(if (name == null) "window.properties" else "window-$name.properties")

    fun load(name: String? = null, minWidth: Int = MIN_WIDTH, minHeight: Int = MIN_HEIGHT): Saved? = runCatching {
        val p = Properties().also { pr -> file(name).inputStream().use { pr.load(it) } }
        Saved(
            p.getProperty("width").toInt().coerceIn(minWidth, 10_000), p.getProperty("height").toInt().coerceIn(minHeight, 10_000),
            p.getProperty("x")?.toIntOrNull(), p.getProperty("y")?.toIntOrNull(), p.getProperty("maximized") == "true",
        )
    }.getOrNull()

    fun save(s: Saved, name: String? = null) {
        runCatching {
            val p = Properties()
            p["width"] = s.width.toString(); p["height"] = s.height.toString(); p["maximized"] = s.maximized.toString()
            s.x?.let { p["x"] = it.toString() }; s.y?.let { p["y"] = it.toString() }
            file(name).outputStream().use { p.store(it, "Backgammon PlyGround window") }
        }
    }
}

/** The View menu's toolbar and side panel switches, remembered between runs. */
object ViewPrefs {
    data class Saved(val panel: Boolean = true, val toolbar: Boolean = true)

    private val file get() = DataDirs.file("view.properties")

    fun load(): Saved = runCatching {
        val p = Properties().also { pr -> file.inputStream().use { pr.load(it) } }
        Saved(p.getProperty("panel") != "false", p.getProperty("toolbar") != "false")
    }.getOrDefault(Saved())

    fun save(s: Saved) {
        runCatching {
            val p = Properties()
            p["panel"] = s.panel.toString(); p["toolbar"] = s.toolbar.toString()
            file.outputStream().use { p.store(it, "Backgammon PlyGround view") }
        }
    }
}
