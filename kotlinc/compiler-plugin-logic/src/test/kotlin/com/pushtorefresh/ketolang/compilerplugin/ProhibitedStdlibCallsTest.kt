package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ProhibitedStdlibCallsTest {

    @Test
    fun `call to prohibited Kotlin package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            import java.io.File

            fun f(s: String): Int {
                File("file").appendText("abc")
                return s.length
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package kotlin.io!, node name = 'no printable name'"
        )
    }

    @Test
    fun `call to prohibited Java package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            import java.io.File

            fun f(s: String): Int {
                File("file").createNewFile()
                return s.length
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package java.io!, node name = 'no printable name'"
        )
    }

    @Test
    fun `constructor call to prohibited Java package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            import java.io.File

            fun f(s: String): Int {
                File("file")
                return s.length
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package java.io!, node name = 'no printable name'"
        )
    }

    @Test
    fun `call to File write is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            import java.io.File

            fun f(s: String): Int {
                File("file").writeText("abc")
                return s.length
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package kotlin.io!, node name = 'no printable name'"
        )
    }

    @Test
    fun `call to File read is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            import java.io.File

            fun f(name: String): String {
                return File(name).readText()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package kotlin.io!, node name = 'no printable name'"
        )
    }

    @Test
    fun `get env var is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                return System.getenv(i.toString())
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package java.lang!, node name = 'no printable name'"
        )
    }

    @Test
    fun `get system time is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                return (System.currentTimeMillis() + i).toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package java.lang!, node name = 'no printable name'"
        )
    }

    @Test
    fun `kotlin reflection is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            data class D(val i: Int)

            fun f(d: D): String {
                return d::class.qualifiedName ?: ""
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: call to prohibited package kotlin.reflect!, node name = 'no printable name'"
        )
    }
}
