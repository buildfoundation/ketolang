package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ExceptionsTest {

    @Test
    fun `throwing exception is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                if (i < 0) {
                    throw Exception("My err")
                }

                return i.toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            " Ketolang error: using 'throw' is not allowed, use 'error(\"text\")'!, node name = 'no printable name'"
        )
    }

    @Test
    fun `catching exception is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(s: String): Int {
                return try {
                    s.toInt()
                } catch (e: Exception) {
                    -1
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: using 'try-catch' is not allowed, all exceptions are fatal!, " +
                    "node name = 'no printable name'"
        )
    }

    @Test
    fun `calling error stdlib fun is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(s: String): Int {
                if (s.isEmpty()) {
                    error("string should not be empty")
                } else {
                    return s.length
                }
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
