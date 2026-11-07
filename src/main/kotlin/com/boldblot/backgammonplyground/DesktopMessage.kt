package com.boldblot.backgammonplyground

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/** The desktop's replacement for Android's Toast: shown as a snackbar by the window (Main.kt). */
object DesktopMessages {
    val flow = MutableSharedFlow<String>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}

fun showMessage(text: String) {
    DesktopMessages.flow.tryEmit(text)
}

/** Percent-encodes like Android's Uri.encode: spaces become %20 (not "+", as URLEncoder writes them). */
fun encodeUri(s: String): String = java.net.URLEncoder.encode(s, Charsets.UTF_8).replace("+", "%20")

/** The CPU's model name, for the speed test. */
fun cpuName(): String = runCatching {
    java.io.File("/proc/cpuinfo").useLines { ls -> ls.firstOrNull { it.startsWith("model name") }?.substringAfter(':')?.trim() }
}.getOrNull() ?: "unknown CPU"

/** Reverses [encodeUri]; a literal "+" stays a "+" (as Android's Uri.decode). */
fun decodeUri(s: String): String = java.net.URLDecoder.decode(s.replace("+", "%2B"), Charsets.UTF_8)
