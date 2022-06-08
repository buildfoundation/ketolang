package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TopLevelPropertiesTest {

    @Test
    fun `var is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Int = 1
            """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: top-level properties are not allowed! Use object class to declare them, node name = 'b'"
        )
    }

    @Test
    fun `val is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Int = 1
            """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: top-level properties are not allowed! Use object class to declare them, node name = 'b'"
        )
    }

    @Test
    fun `const val is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: Int = 1
            """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: top-level properties are not allowed! Use object class to declare them, node name = 'b'"
        )
    }
}
