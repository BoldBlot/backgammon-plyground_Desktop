package com.boldblot.backgammonplyground.engine

import com.boldblot.backgammonplyground.core.EngineKind
import com.boldblot.backgammonplyground.core.PureTdNet
import org.junit.Assert.assertEquals
import org.junit.Test

/** What an analysis made through the service's views records as its engine: PureTD's name says which network. */
class EngineNameTest {
    @Test
    fun viewsNameTheActiveNetwork() {
        val service = EngineService()
        assertEquals("PureTD", service.analysis.name)
        service.net = PureTdNet.COMPACT
        assertEquals("PureTD 4L", service.analysis.name)
        assertEquals("PureTD 4L", service.of(EngineKind.PURETD).name)
        assertEquals("GNU BG", service.of(EngineKind.GNUBG).name)
    }
}
