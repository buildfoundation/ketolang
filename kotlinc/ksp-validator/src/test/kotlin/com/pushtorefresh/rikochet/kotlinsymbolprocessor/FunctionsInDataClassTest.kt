package com.pushtorefresh.rikochet.kotlinsymbolprocessor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FunctionsInDataClassTest {

    @Test
    fun `valid top-level function is not allowed in data class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val v: Int) {
                fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Rikochet error: functions in classes are not allowed!, node name = 'f'"
        )
    }
}
