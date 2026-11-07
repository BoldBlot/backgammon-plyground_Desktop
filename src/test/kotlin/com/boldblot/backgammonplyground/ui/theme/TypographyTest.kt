package com.boldblot.backgammonplyground.ui.theme

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTest {
    @Test
    fun noStyleIsSmallerThanTheMinimum() {
        val t = DesktopTypography
        val styles = listOf(
            t.displayLarge, t.displayMedium, t.displaySmall, t.headlineLarge, t.headlineMedium, t.headlineSmall,
            t.titleLarge, t.titleMedium, t.titleSmall, t.bodyLarge, t.bodyMedium, t.bodySmall,
            t.labelLarge, t.labelMedium, t.labelSmall,
        )
        styles.forEach { assertTrue("${it.fontSize}", it.fontSize.value >= MIN_TEXT.value) }
    }

    /** Sizes written directly in the code, which the theme doesn't cover. */
    @Test
    fun noFixedSizeInTheCodeIsSmallerThanTheMinimum() {
        val size = Regex("""fontSize = (\d+(\.\d+)?)\.sp""")
        val tooSmall = File("src/main/kotlin").walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            size.findAll(file.readText()).filter { it.groupValues[1].toFloat() < MIN_TEXT.value }.map { "${file.name}: ${it.value}" }
        }.toList()
        assertEquals(emptyList<String>(), tooSmall)
    }
}
