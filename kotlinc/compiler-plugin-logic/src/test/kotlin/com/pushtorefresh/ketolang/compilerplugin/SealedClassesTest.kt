package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SealedClassesTest {

    @Test
    fun `sealed class with data class is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            sealed class C
            data class D(val i: Int): C()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `sealed class with object is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            sealed class C
            object A: C()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `sealed class with regular class is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            sealed class C
            class A: C()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: regular classes are not allowed, only data classes and enums are allowed!, node name = 'A'"
        )
    }

    @Test
    fun `sealed class as function parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            sealed class C
            object A: C()

            fun f(c: C): String {
                return c.toString()
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `when statement with is check for sealed class is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            sealed class C
            object A: C()
            data class D(val i: Int): C()

            fun x(c: C): Int {
                return when (c) {
                    is A -> 0
                    is D -> c.i
                }
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
