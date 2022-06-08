package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DataClassAsObjectPropertiesTest {

    @Test
    fun `val data class is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            object A {
                val d = D(1)
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var data class is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            object A {
                var d = D(1)
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'd'"
        )
    }

    @Test
    fun `val List(data class) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            object A {
                val l: List<D> = listOf(D(1))
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var List(data class) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            object A {
                var l: List<D> = listOf(D(1))
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'l'"
        )
    }

    @Test
    fun `val MutableList(data class) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            object A {
                val l: MutableList<D> = mutableListOf(D(1))
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'l'"
        )
    }
}
