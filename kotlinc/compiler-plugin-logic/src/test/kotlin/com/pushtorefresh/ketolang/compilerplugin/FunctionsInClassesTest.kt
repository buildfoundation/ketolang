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
            package p

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
            "Ketolang error: functions in classes are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in enum`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

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
            "Ketolang error: functions in classes are not allowed!, node name = 'f'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in a class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

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
            "Ketolang error: regular classes are not allowed, only data classes and enums are allowed!, node name = 'C'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in interface`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

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
            "Ketolang error: abstract classes and interfaces are not allowed!, node name = 'I'"
        )
    }

    @Test
    fun `valid top-level function is not allowed in abstract class`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

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
            "Ketolang error: abstract classes and interfaces are not allowed!, node name = 'C'"
        )
    }
}
