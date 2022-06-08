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
            object A {
                val l1: List<Int> = mutableListOf()
                val l2: List<Int> = (if ((l1 as MutableList<Int>).add(2)) l1 else emptyList())
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
    fun `explicit casting in property initializer is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            object A {
                val l1: List<Int> = mutableListOf()
                val l2: List<Int> = l1 as MutableList<Int>
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
    fun `explicit casting in function via generics is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun <T> List<*>.okay() = this as T
            val l1 : List<String> = mutableListOf()
            fun f(i: Int): Int {
                l1.okay<MutableList<String>>().add("abc")
                return i
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: functions accepting mutable parameters are not allowed!, node name = 'okay'"
        )
    }

    @Test
    fun `casting in function via reflection is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            inline fun <reified T: Any, V: Any> List<V>.okay() = T::class.cast(this)
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Unresolved reference: cast"
        )
    }

    @Test
    fun `casting via inline function is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            inline fun <reified T: Any, V: Any> List<V>.okay() = this as T
            fun lol(i: Int): Int {
                val l1 : List<String> = mutableListOf()
                l1.okay<MutableList<String>, String>().add("abc")
                return i
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: functions accepting mutable parameters are not allowed!, node name = 'okay'"
        )
    }

    @Test
    fun `implicit casting via when statement is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(i: Int): Int  {
                val x = if (i > 10) i.toString() else i

                return when (x) {
                    is String -> (x + "2").toInt()
                    else -> -1
                }
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `implicit casting to mutable type via when statement is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(l: List<Int>): List<Int>  {
                return when (l) {
                    is MutableList -> { l.add(2); l }
                    else -> l
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: type check for mutable collection types is not allowed!, node name = 'no printable name'"
        )
    }
}
