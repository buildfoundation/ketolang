package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CastTest {

    @Test
    fun `explicit casting in function is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(l: List<Int>): Int {
                val m = l as MutableList<Int>
                m.add(1)
                return m.size
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: explicit casting is prohibited!, node name = 'no printable name'"
        )
    }

    @Test
    fun `explicit safe casting in function is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(l: List<Int>): Int {
                val m = l as? MutableList<Int>
                m?.add(1)
                return m?.size ?: 0
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: explicit casting is prohibited!, node name = 'no printable name'"
        )
    }

    @Test
    fun `explicit casting in conditional property initializer is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val l1: List<Int> = mutableListOf()
            val l2: List<Int> = (if ((l1 as MutableList<Int>).add(2)) l1 else emptyList())
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: explicit casting is prohibited!, node name = 'no printable name'"
        )
    }

    @Test
    fun `explicit casting in property initializer is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val l1: List<Int> = mutableListOf()
            val l2: List<Int> = l1 as MutableList<Int>
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: explicit casting is prohibited!, node name = 'no printable name'"
        )
    }
}
