package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FunctionsInClassesTest {

    @Test
    fun `valid top-level function is not allowed in data class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val v: Int) {
                fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions in classes are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in enum`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            enum class E {
                A,B,C;
                
            fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: functions in classes are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            class C {
                fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: regular classes are not allowed, only data classes and enums are allowed!, node name = 'C'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in interface`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            interface I {
                fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: abstract classes and interfaces are not allowed!, node name = 'I'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in abstract class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            abstract class C {
                fun f(a: String): Int {
                    return a.length
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: abstract classes and interfaces are not allowed!, node name = 'C'"
        )
    }
}
