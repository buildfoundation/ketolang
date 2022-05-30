package com.pushtorefresh.rikochet.kotlinsymbolprocessor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ArrayDataClassPropertiesTest {

    @Test
    fun `var Array is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(var b: Array<Int>)
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Rikochet error: mutable properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val Array is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val b: Array<Int>)
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Rikochet error: array properties are not allowed because arrays are mutable, node name = 'b'"
        )
    }
}
