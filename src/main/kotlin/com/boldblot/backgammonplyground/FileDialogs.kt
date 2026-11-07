package com.boldblot.backgammonplyground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

/** The system file dialogs (AWT's FileDialog; a Swing chooser for folders). They block until closed: call them from the UI thread. */
object FileDialogs {
    fun open(title: String): File? {
        val d = FileDialog(null as Frame?, title, FileDialog.LOAD)
        d.isVisible = true
        return d.file?.let { File(d.directory, it) }
    }

    fun save(title: String, suggestedName: String): File? {
        val d = FileDialog(null as Frame?, title, FileDialog.SAVE)
        d.file = suggestedName
        d.isVisible = true
        return d.file?.let { File(d.directory, it) }
    }

    fun folder(title: String): File? {
        val c = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }
        return if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) c.selectedFile else null
    }
}

/** What Android's activity-result launchers were: call [launch] to show the dialog; the result goes to the callback. */
class FileLauncher<I>(private val action: (I) -> Unit) {
    fun launch(input: I) = action(input)
}

@Composable
fun rememberOpenFile(title: String, onPicked: (File) -> Unit): FileLauncher<Unit> {
    val cb = rememberUpdatedState(onPicked)
    return remember(title) { FileLauncher<Unit> { FileDialogs.open(title)?.let(cb.value) } }
}

/** The input is the suggested file name. */
@Composable
fun rememberSaveFile(title: String, onPicked: (File) -> Unit): FileLauncher<String> {
    val cb = rememberUpdatedState(onPicked)
    return remember(title) { FileLauncher<String> { name -> FileDialogs.save(title, name)?.let(cb.value) } }
}

@Composable
fun rememberPickFolder(title: String, onPicked: (File) -> Unit): FileLauncher<Unit> {
    val cb = rememberUpdatedState(onPicked)
    return remember(title) { FileLauncher<Unit> { FileDialogs.folder(title)?.let(cb.value) } }
}
