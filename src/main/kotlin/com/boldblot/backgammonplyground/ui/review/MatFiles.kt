package com.boldblot.backgammonplyground.ui.review

import com.boldblot.backgammonplyground.FileDialogs
import com.boldblot.backgammonplyground.showMessage
import com.boldblot.backgammonplyground.core.GameState
import com.boldblot.backgammonplyground.core.MatExport
import com.boldblot.backgammonplyground.core.MatchRules
import com.boldblot.backgammonplyground.core.Opponent
import com.boldblot.backgammonplyground.data.GameEntity
import com.boldblot.backgammonplyground.data.MatchEntity
import java.io.File

/** Sharing and saving exported .mat files. */
object MatFiles {
    /** Not text/plain: document providers would append ".txt" to the file name. */
    const val MIME = "application/octet-stream"

    /** Asks where to save [content] as a file called [name], and writes it. Returns whether it was saved. */
    fun share(name: String, content: String): Boolean {
        val file = FileDialogs.save("Export $name", name) ?: return false
        file.writeText(content)
        showMessage("Saved ${file.name}.")
        return true
    }

    /**
     * A match, or the games of it in [only], in the .mat format read by eXtreme Gammon and GNU
     * Backgammon: file name and content.
     */
    /** [playerName]: the player's name in the file (Settings), cleaned by [fileName]. */
    fun matchFile(
        match: MatchEntity, games: List<Pair<GameEntity, GameState>>, only: Pair<GameEntity, GameState>? = null,
        playerName: String = "Player", humanFirst: Boolean = false,
    ): Pair<String, String> {
        val selected = if (only != null) listOf(only) else games
        val info = MatExport.Info(
            rules = MatchRules(match.length, match.useCube, match.jacoby, match.crawford),
            // By player index: human, computer. Short names, as XG and gnubg show them in narrow columns.
            names = listOf(fileName(playerName), Opponent.name(match.cpuLevel)),
            startedAt = match.startedAt, humanFirst = humanFirst,
        )
        val out = selected.map { (e, g) -> MatExport.Game(e.number, listOf(e.scoreBeforeHuman, e.scoreBeforeCpu), g) }
        val stamp = java.text.SimpleDateFormat("yyyy-MM-dd-HHmm", java.util.Locale.ROOT).format(java.util.Date(match.startedAt))
        val name = "PlyGround-$stamp-" + if (only == null) "match.mat" else "game${out.first().number}.mat"
        return name to MatExport.write(info, out)
    }

    /** A name safe for the .mat header: no colons, quotes or line breaks, at most 30 characters; "Player" if empty. */
    fun fileName(name: String): String =
        name.replace(Regex("[:\"\r\n\t]"), " ").replace(Regex(" +"), " ").trim().take(30).trim().ifEmpty { "Player" }

    /** Several files, named uniquely (two matches started in the same minute get the match id added). */
    fun unique(files: List<Pair<String, String>>): List<Pair<String, String>> {
        val seen = HashSet<String>()
        return files.mapIndexed { i, (name, text) ->
            val n = if (seen.add(name)) name else name.removeSuffix(".mat") + "-${i + 1}.mat".also { seen.add(it) }
            n to text
        }
    }

    /** Saves several files: one asks for a file name, more ask for a folder. */
    fun shareMany(files: List<Pair<String, String>>) {
        if (files.size == 1) { share(files[0].first, files[0].second); return }
        val dir = FileDialogs.folder("Save ${files.size} matches to a folder") ?: return
        val n = saveToFolder(dir, files)
        showMessage("Saved $n of ${files.size} matches.")
    }

    /** Writes the files into [dir]. Returns how many were written. */
    fun saveToFolder(dir: File, files: List<Pair<String, String>>): Int {
        var n = 0
        for ((name, content) in files) if (runCatching { File(dir, name).writeText(content) }.isSuccess) n++
        return n
    }

    /** Writes [content] to [file]. */
    fun save(file: File, content: String) {
        file.writeText(content)
    }
}
