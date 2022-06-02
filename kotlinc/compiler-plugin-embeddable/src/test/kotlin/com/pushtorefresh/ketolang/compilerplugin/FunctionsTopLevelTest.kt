package com.pushtorefresh.ketolang.compilerplugin

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
            "Ketolang error: functions returning Unit are not allowed!, node name = 'f'"
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
            "Ketolang error: functions returning Any are not allowed!, node name = 'f'"
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
            "Ketolang error: functions without parameters are not allowed!, node name = 'f'"
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
            "Ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
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
    fun `data class parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
            fun f(d: D): String {
                return d.toString()
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
    fun `List(data class) parameter is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)

            fun f(b: List<D>): String {
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
            "Ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
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
            "Ketolang error: functions accepting mutable parameters are not allowed!, node name = 'f'"
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
            "Ketolang error: suspend functions are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `call to prohibited Kotlin package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
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
}
