package com.boldblot.backgammonplyground.engine

import com.boldblot.backgammonplyground.core.AnalysisEngine
import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CpuLevel
import com.boldblot.backgammonplyground.core.CubeContext
import com.boldblot.backgammonplyground.core.EngineKind
import com.boldblot.backgammonplyground.core.MatchEquityTable
import com.boldblot.backgammonplyground.core.PureTdNet
import com.boldblot.backgammonplyground.core.Move
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.EnumMap
import com.boldblot.backgammonplyground.AppLog
import java.util.concurrent.Executors

/**
 * Owns the engines (PureTD, GNU Backgammon, Open Sage). They are not thread-safe, so every call
 * goes through [run] (or the [analysis] / [of] views called inside it), which executes on one
 * dedicated thread, the game's slot (the deeper searches spread their own work over
 * [gameThreads] cores). PureTD loads at start; the others the first time they are used.
 *
 * The background analysis has a slot of its own when PureTD analyzes ([twoSlots]): a second
 * PureTD on the same networks, on its own thread ([runBackground]) with [backgroundThreads] and
 * [backgroundLow] priority, so the game never waits for it. GNU Backgammon (globals) and Open
 * Sage (one thread pool for all its copies) can't have a second copy.
 */
class EngineService() {

    /** Single engine thread (the C searches keep large buffers on the heap, not the stack). */
    val dispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(null, r, "engines", 4L * 1024 * 1024).apply { isDaemon = true }
    }.asCoroutineDispatcher()

    /** PureTD's network size for the game and the analysis (Advanced engine settings); the other net loads on first use. */
    @Volatile var net: PureTdNet = PureTdNet.WIDE

    /** Money checker plays scored with the cubeful network ([PureTdEngine.cubefulCheckers]), for every PureTD. */
    @Volatile var cubefulCheckers: Boolean = true

    /** One PureTD (handle and engine) per network size, opened on first use on the engine thread. */
    private class Ptd(val handle: Long, val engine: PureTdEngine)
    private val ptds = EnumMap<PureTdNet, Ptd>(PureTdNet::class.java)

    /** The active network's engine. Call after [start], on the engine thread. */
    val engine: PureTdEngine get() = ptd(net).engine
    private val ptdHandle: Long get() = ptd(net).handle
    private var sageHandle = 0L

    /** Cores the phone has online (Android tells any app, no permission needed). */
    val cores: Int = Runtime.getRuntime().availableProcessors()

    /** Threads for the game's searches and for the background analysis (Advanced engine settings). */
    @Volatile var gameThreads: Int = cores
    @Volatile var backgroundThreads: Int = defaultBackgroundThreads(cores)
    /** The background analysis runs at low priority: Android gives the game's searches the cores first. */
    @Volatile var backgroundLow: Boolean = true

    /** The background analysis has its own slot (only PureTD can have a second copy). */
    val twoSlots: Boolean get() = analysisKind == EngineKind.PURETD

    /** The background slot's thread and its PureTD (on the game PureTD's networks). */
    // Linux lets a thread raise its nice value but not lower it again, so low and normal priority are two threads
    // (made on first use): the low one is niced when it starts, and the search threads it makes inherit that.
    private fun backgroundThread(low: Boolean): CoroutineDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(null, {
            if (low) {
                val rc = PureTdNative.nativeSetThreadNice(BACKGROUND_NICE)
                if (rc != 0) AppLog.i("Could not lower the analysis priority (error $rc)")
            }
            r.run()
        }, if (low) "analysis-low" else "analysis", 4L * 1024 * 1024).apply { isDaemon = true }
    }.asCoroutineDispatcher()
    private val backgroundLowDispatcher by lazy { backgroundThread(true) }
    private val backgroundNormalDispatcher by lazy { backgroundThread(false) }
    private val backgroundDispatcher: CoroutineDispatcher get() = if (backgroundLow) backgroundLowDispatcher else backgroundNormalDispatcher
    private val backgrounds = EnumMap<PureTdNet, Ptd>(PureTdNet::class.java)

    private val engines = EnumMap<EngineKind, SearchEngine>(EngineKind::class.java)

    private val ready = CompletableDeferred<Unit>()

    /** The engine that analyzes (Settings → Analysis), for [analysis]. */
    @Volatile var analysisKind: EngineKind = EngineKind.PURETD

    /** [kind]'s engine, loaded on first use. Call on the engine thread. */
    fun get(kind: EngineKind): SearchEngine = if (kind == EngineKind.PURETD) engine.also { it.cubefulCheckers = cubefulCheckers } else engines.getOrPut(kind) {
        when (kind) {
            EngineKind.PURETD -> error("PureTD is opened by ptd()")
            EngineKind.GNUBG -> GnubgEngine.open(GnubgNative.files().path)
            EngineKind.SAGE -> {
                val dir = SageNative.files()
                val handle = SageNative.nativeOpen(
                    SageNative.NETWORKS.map { File(dir, it).path }.toTypedArray(), SageNative.HIDDEN,
                    File(dir, SageNative.BEAROFF).path, Runtime.getRuntime().availableProcessors(),
                )
                check(handle != 0L) { "Open Sage engine failed to load its networks" }
                sageHandle = handle
                SageEngine(handle)
            }
        }
    }

    /**
     * [kind]'s engine for objects created before it has loaded (a screen restored straight into a
     * game): each call looks the engine up when it is made. Calls go through [run], which waits for it.
     */
    fun view(kind: () -> EngineKind): AnalysisEngine = object : AnalysisEngine {
        private val e get() = get(kind())
        // PureTD's name says which network, as the engine's own does: "PureTD" or "PureTD 4L".
        override val name: String get() = kind().let { if (it == EngineKind.PURETD) net.engineName else it.label }
        override fun rankMoves(board: Board, ctx: CubeContext, d1: Int, d2: Int, plies: Int, played: Move?) = e.rankMoves(board, ctx, d1, d2, plies, played)
        override fun chooseMove(board: Board, ctx: CubeContext, d1: Int, d2: Int, level: CpuLevel) = e.chooseMove(board, ctx, d1, d2, level)
        override fun bestPlayChances(board: Board, ctx: CubeContext, d1: Int, d2: Int) = e.bestPlayChances(board, ctx, d1, d2)
        override fun cubeDecision(board: Board, ctx: CubeContext, plies: Int) = e.cubeDecision(board, ctx, plies)
        override fun cubeDecisionAtLevel(board: Board, ctx: CubeContext, level: CpuLevel) = e.cubeDecisionAtLevel(board, ctx, level)
        override fun cachedCube(board: Board, ctx: CubeContext, plies: Int) = e.cachedCube(board, ctx, plies)
        override fun luck(board: Board, ctx: CubeContext, d1: Int, d2: Int, firstRoll: Boolean) = e.luck(board, ctx, d1, d2, firstRoll)
        override fun positionId(board: Board, onRoll: Int) = e.positionId(board, onRoll)
        override fun matchId(ctx: CubeContext, d1: Int, d2: Int) = e.matchId(ctx, d1, d2)
    }

    /** The analysis engine ([analysisKind]). */
    val analysis: AnalysisEngine = view { analysisKind }

    /** [kind]'s engine (the computer's in a match, or one chosen for an analysis). */
    fun of(kind: EngineKind): AnalysisEngine = view { kind }

    /** Extracts data files (first launch / after an update) and initializes PureTD. */
    suspend fun start() {
        if (ready.isCompleted) return
        withContext(dispatcher) {
            if (ready.isCompleted) return@withContext
            PureTdNative.load()
            ptd(net)
            ready.complete(Unit)
        }
    }

    /** [n]'s PureTD, opened on first use (the bear-off database is built once, about a second, and cached). */
    @Synchronized private fun ptd(n: PureTdNet): Ptd = ptds.getOrPut(n) {
        val dir = PureTdNative.files()
        val handle = PureTdNative.nativeOpen(
            File(dir, n.cubeless).path, File(dir, n.cubeful).path,
            File(EngineFiles.writableDir(), "puretd-bearoff.bin").path, Runtime.getRuntime().availableProcessors(),
        )
        check(handle != 0L) { "PureTD engine failed to load its networks" }
        Ptd(handle, PureTdEngine(handle, netName = n.engineName))
    }

    /** Runs [block] on the engine thread with [kind]'s engine (the analysis engine by default). */
    suspend fun <T> run(kind: EngineKind? = null, block: SearchEngine.() -> T): T {
        start()
        return withContext(dispatcher) {
            val threads = gameThreads.coerceIn(1, cores)
            PureTdNative.nativeSetThreads(ptdHandle, threads)
            if (sageHandle != 0L) SageNative.nativeSetThreads(sageHandle, threads)
            get(kind ?: analysisKind).block()
        }
    }

    /**
     * Runs [block] in the background analysis slot ([twoSlots] only): the second PureTD, on its own
     * thread. Its search threads start from that thread, so they share its priority.
     */
    suspend fun <T> runBackground(block: SearchEngine.() -> T): T {
        start()
        return withContext(backgroundDispatcher) {
            val threads = backgroundThreads.coerceIn(1, cores)
            val n = net
            val b = backgrounds[n] ?: openBackground(n, threads)
            PureTdNative.nativeSetThreads(b.handle, threads)
            b.engine.cubefulCheckers = cubefulCheckers
            b.engine.block()
        }
    }

    private fun openBackground(n: PureTdNet, threads: Int): Ptd {
        val handle = PureTdNative.nativeShare(ptd(n).handle, threads)
        check(handle != 0L) { "PureTD background engine failed" }
        // Its own match equity table: the table fills a cache as it goes, which two threads can't share.
        return Ptd(handle, PureTdEngine(handle, MatchEquityTable(), n.engineName)).also { backgrounds[n] = it }
    }

    companion object {
        const val BACKGROUND_NICE = 10
        /** The background analysis' default: a quarter of the cores, at least 1. */
        fun defaultBackgroundThreads(cores: Int) = (cores / 4).coerceAtLeast(1)
    }
}
