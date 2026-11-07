package com.boldblot.backgammonplyground.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class RowSelectionTest {
    private val order = listOf(1, 2, 3, 4, 5)

    @Test
    fun aPlainPressSelectsOnlyThatRow() {
        val s = RowSelection(setOf(1, 2), 1).press(4, order, ctrl = false, shift = false)
        assertEquals(setOf(4), s.selected)
        assertEquals(4, s.anchor)
    }

    @Test
    fun ctrlAddsAndRemoves() {
        val added = RowSelection(setOf(1), 1).press(3, order, ctrl = true, shift = false)
        assertEquals(setOf(1, 3), added.selected)
        assertEquals(setOf(1), added.press(3, order, ctrl = true, shift = false).selected)
    }

    @Test
    fun shiftSelectsTheRangeFromTheAnchorInEitherDirection() {
        assertEquals(setOf(2, 3, 4), RowSelection(setOf(2), 2).press(4, order, ctrl = false, shift = true).selected)
        assertEquals(setOf(2, 3, 4), RowSelection(setOf(4), 4).press(2, order, ctrl = false, shift = true).selected)
    }

    @Test
    fun shiftMovesTheRangeButKeepsTheAnchor() {
        val first = RowSelection(setOf(2), 2).press(5, order, ctrl = false, shift = true)
        assertEquals(setOf(1, 2), first.press(1, order, ctrl = false, shift = true).selected)
    }

    @Test
    fun ctrlShiftAddsTheRangeToTheSelection() {
        val s = RowSelection(setOf(1, 3), 3).press(4, order, ctrl = true, shift = true)
        assertEquals(setOf(1, 3, 4), s.selected)
    }

    @Test
    fun shiftWithoutAnAnchorSelectsOnlyThatRow() {
        assertEquals(setOf(3), RowSelection<Int>().press(3, order, ctrl = false, shift = true).selected)
    }

    @Test
    fun rowsThatAreGoneLeaveTheSelection() {
        val s = RowSelection(setOf(1, 2), 2).keepOnly(listOf(1, 3))
        assertEquals(setOf(1), s.selected)
        assertEquals(null, s.anchor)
    }
}
