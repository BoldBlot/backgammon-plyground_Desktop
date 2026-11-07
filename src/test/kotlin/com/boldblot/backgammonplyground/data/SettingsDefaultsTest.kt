package com.boldblot.backgammonplyground.data

import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDefaultsTest {
    @Test
    fun theComputersDecisionsAreAnalyzedByDefault() = assertTrue(Settings().cpuAnalysis)
}
