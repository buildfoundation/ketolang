package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LoopsTest {

    @Test
    fun `while loop is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                var x = i
                var s = ""
                while (x < 0) {
                    x--
                    s+=x.toString()
                }

                return s
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: using loops is not allowed! Use Kotlin collections/streaming API, " +
                    "node name = 'no printable name'"
        )
    }

    @Test
    fun `for loop is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                var s = ""
                for (x in (0..i)) {
                    s+=x.toString()
                }

                return s
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: using loops is not allowed! Use Kotlin collections/streaming API, " +
                    "node name = 'no printable name'"
        )
    }

    @Test
    fun `stream foreach is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                var s = ""
                (0..i).forEach { 
                    s+=it.toString()
                 }

                return s
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
