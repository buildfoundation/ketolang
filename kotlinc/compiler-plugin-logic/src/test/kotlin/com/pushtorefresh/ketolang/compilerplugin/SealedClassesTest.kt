package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SealedClassesTest {

    @Test
    fun `sealed class with data class allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            sealed class C
            data class D(val i: Int): C()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `sealed class with object allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
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
}
