package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class NoPackageTest {

    @Test
    fun `object without a package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            object MyStuff {
                const val s = "abc"
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: class must be declared in a named package!, node name = 'MyStuff'"
        )
    }

    @Test
    fun `data class without a package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int)
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: class must be declared in a named package!, node name = 'D'"
        )
    }

    @Test
    fun `function without a package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            fun f(i: Int): Int { return i + 1; }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: function must be declared in a named package!, node name = 'f'"
        )
    }

    @Test
    fun `property without a package is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val i = 1
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: property must be declared in a named package!, node name = 'i'"
        )
    }
}
