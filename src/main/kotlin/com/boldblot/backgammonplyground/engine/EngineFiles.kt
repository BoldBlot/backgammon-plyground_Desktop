package com.boldblot.backgammonplyground.engine

import com.boldblot.backgammonplyground.DataDirs
import java.io.File

/** The engines' data files, read in place from the bundle (no unpacking), and the native library. */
object EngineFiles {
    /** The bundled data folder: `compose.application.resources.dir`, else `engine-data` in the working directory (development). */
    private val dataRoot: File by lazy {
        System.getProperty("compose.application.resources.dir")?.let { File(it, "engine-data") }?.takeIf { it.isDirectory }
            ?: File("engine-data")
    }

    fun dataDir(folder: String): File = File(dataRoot, folder)

    /** Files the engines generate themselves (PureTD's bearoff table). */
    fun writableDir(): File = DataDirs.data

    /** The bundled native libraries: `native/` in the same place as the data. */
    private val libDir: File get() = File(dataRoot.parentFile ?: File("."), "native")

    /**
     * The library level this CPU can run: "x86-64-v3" needs AVX2, FMA, BMI1, BMI2, F16C, LZCNT, MOVBE and AVX
     * (the x86-64-v3 feature level); otherwise the baseline "x86-64". [flags] are the CPU's /proc/cpuinfo flags.
     */
    fun cpuLevel(flags: Set<String>): String {
        val v3 = listOf("avx", "avx2", "bmi1", "bmi2", "fma", "f16c", "abm", "movbe")
        // The kernel names LZCNT "abm" and BMI1 "bmi1".
        return if (v3.all { it in flags }) "x86-64-v3" else "x86-64"
    }

    private fun hostFlags(): Set<String> = runCatching {
        File("/proc/cpuinfo").useLines { ls ->
            ls.firstOrNull { it.startsWith("flags") }?.substringAfter(':')?.trim()?.split(Regex("\\s+"))?.toSet().orEmpty()
        }
    }.getOrDefault(emptySet())

    /** The one native library with all three engines. */
    private var loaded = false

    /** The level of the loaded library, for the speed test and the log. */
    @Volatile var loadedLevel: String? = null
        private set

    /**
     * Loads libbgjni from [libraryPath], `-Dbgjni.library=...`, or the bundled `native/libbgjni-<level>.so`
     * chosen by CPU detection (`-Dbgjni.level=` forces a level).
     */
    @Synchronized
    fun load(libraryPath: String? = null) {
        if (loaded) return
        val explicit = libraryPath ?: System.getProperty("bgjni.library")
        if (explicit != null) {
            System.load(explicit)
            loadedLevel = Regex("libbgjni-(.*)\\.so").find(File(explicit).name)?.groupValues?.get(1) ?: "unknown"
        } else {
            val level = System.getProperty("bgjni.level") ?: cpuLevel(hostFlags())
            val lib = File(libDir, "libbgjni-$level.so")
            check(lib.isFile) { "The engine library is missing: $lib" }
            System.load(lib.absolutePath)
            loadedLevel = level
        }
        loaded = true
        com.boldblot.backgammonplyground.AppLog.i("Engine library: $loadedLevel")
    }
}
