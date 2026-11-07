package com.boldblot.backgammonplyground.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class BackLabelTest {
    @Test
    fun theButtonNamesTheViewItGoesBackTo() {
        assertEquals("Back to game", backLabel("game"))
        assertEquals("Back to start", backLabel(START_ROUTE))
        assertEquals("Back to review", backLabel("review/{id}?game={game}&mistakes={mistakes}&marks={marks}&markedOnly={markedOnly}"))
        assertEquals("Back to quiz", backLabel("positions/quiz/{spec}"))
        assertEquals("Back", backLabel("position"))
        assertEquals("Back", backLabel(null))
    }
}
