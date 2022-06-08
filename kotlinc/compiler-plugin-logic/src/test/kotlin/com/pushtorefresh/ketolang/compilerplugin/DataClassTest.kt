package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DataClassTest {

    @Test
    fun `data class(val Int) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(val i: Int)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `data class(data class) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D1(val i: Int)
            data class D2(val d: D1)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `data class(var Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(var i: Int)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'i'"
        )
    }

    @Test
    fun `data class (val List(Int)) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(val l: List<Int>)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `data class (val List(data class)) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D1(val i: Int)
            data class D2(val l: List<D1>)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `data class (var List(Int)) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(var l: List<Int>)
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
    fun `data class (var List(Any)) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(var l: List<Any>)
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
    fun `data class (val MutableList(Int)) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(val l: MutableList<Int>)
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
