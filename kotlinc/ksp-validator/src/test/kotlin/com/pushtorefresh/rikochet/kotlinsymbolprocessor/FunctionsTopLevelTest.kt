package com.pushtorefresh.ketolang.kotlinsymbolprocessor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FunctionsTopLevelTest {

    @Test
    fun `return type Unit is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f() {
                
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions returning Unit are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `return type Any is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(): Any {
                return Unit
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions returning Any are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `0 parameters are not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(): Int {
                return 0
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions without parameters are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `Array parameter is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(array: Array<String>): Int {
                return array.size
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `Int parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(b: Int): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `String parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(b: String): String {
                return b
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `List(String) parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(b: List<String>): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `List(Any) parameter is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(b: List<Any>): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `MutableList(String) parameter is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(b: MutableList<Any>): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `suspend function is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            suspend fun f(b: Int): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: suspend functions are not allowed!, node name = 'f'"
        )
    }
}
