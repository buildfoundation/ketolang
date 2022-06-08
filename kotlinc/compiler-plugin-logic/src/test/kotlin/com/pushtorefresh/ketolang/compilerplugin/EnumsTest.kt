package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EnumsTest {

    @Test
    fun `enum is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E {
                A,B,C
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `enum with var Int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(var a: Int) {
                A(1),B(2),C(3)
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'a'"
        )
    }

    @Test
    fun `enum with val Int is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(val a: Int) {
                A(1),B(2),C(3)
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `enum with val List(Int) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(val a: List<Int>) {
                A(listOf(1)),B(listOf(2)),C(listOf(3))
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `enum with val List(data class) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(val i: Int)

            enum class E(val a: List<Int>) {
                A(listOf(1)),B(listOf(2)),C(listOf(3))
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `enum with var List(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(var a: List<Int>) {
                A(listOf(1)),B(listOf(2)),C(listOf(3))
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'a'"
        )
    }

    @Test
    fun `enum with var List(Any) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(var a: List<Any>) {
                A(listOf(1)),B(listOf(2)),C(listOf(3))
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'a'"
        )
    }

    @Test
    fun `enum with var MutableList(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            enum class E(var a: MutableList<Int>) {
                A(mutableListOf(1)),B(mutableListOf(2)),C(mutableListOf(3))
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'a'"
        )
    }
}
