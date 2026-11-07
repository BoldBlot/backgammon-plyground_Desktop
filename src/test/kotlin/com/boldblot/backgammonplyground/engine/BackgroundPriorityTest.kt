package com.boldblot.backgammonplyground.engine

import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CubeContext
import com.boldblot.backgammonplyground.core.CubeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import kotlin.concurrent.thread

/** The background analysis' nice level reaches PureTD's search threads (needs the host JNI library; Linux only). */
class BackgroundPriorityTest {

    /** The nice value of every thread of this process (field 19 of /proc/self/task/<tid>/stat). */
    private fun nices(): Map<String, Int> =
        File("/proc/self/task").listFiles().orEmpty().mapNotNull { t ->
            runCatching {
                val stat = File(t, "stat").readText()
                // The command name is in parentheses and may hold spaces: count the fields after the last ")".
                stat.substringAfterLast(')').trim().split(' ')[16].toInt().let { t.name to it }
            }.getOrNull()
        }.toMap()

    @Test
    fun niceLevelIsInheritedBySearchThreads() {
        val engine = TestEngine.engine
        assumeTrue("needs the host JNI library", engine != null && File("/proc/self/task").isDirectory)
        val before = nices()
        var rc = -1
        val ctx = CubeContext(0, listOf(0, 0), false, true, CubeState(), 0)
        val worker = thread(name = "niced-search") {
            rc = PureTdNative.nativeSetThreadNice(EngineService.BACKGROUND_NICE)
            // 3-ply: long enough to sample the search threads that each search creates.
            engine!!.rankMoves(Board.INITIAL, ctx, 6, 4, 3, null)
        }
        var niced = 0
        val deadline = System.currentTimeMillis() + 20_000
        while (worker.isAlive && System.currentTimeMillis() < deadline) {
            niced = maxOf(niced, nices().filterKeys { it !in before }.count { it.value == EngineService.BACKGROUND_NICE })
            Thread.sleep(20)
        }
        worker.join()
        assertEquals("setpriority failed", 0, rc)
        assertTrue("the search worker threads should inherit nice ${EngineService.BACKGROUND_NICE}, saw $niced", niced >= 2)
        // The JVM's own threads are untouched.
        assertTrue(before.values.all { it == 0 })
    }
}
