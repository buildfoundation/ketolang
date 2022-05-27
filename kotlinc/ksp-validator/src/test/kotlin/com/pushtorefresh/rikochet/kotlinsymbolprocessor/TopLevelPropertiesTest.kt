package com.pushtorefresh.rikochet.kotlinsymbolprocessor

import com.pushtorefresh.rikochet.kotlincsymbolprocessor.RikochetSymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TopLevelPropertiesTest {

    @Test
    fun `mutable int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Int = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "Rikochet error: primitive and String properties must be declared as 'const', node name = 'b'")
    }

    @Test
    fun `mutable long is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Long = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "Rikochet error: primitive and String properties must be declared as 'const', node name = 'b'")
    }

    @Test
    fun `mutable short is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Short = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "Rikochet error: primitive and String properties must be declared as 'const', node name = 'b'")
    }

    @Test
    fun `mutable byte is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Byte = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "Rikochet error: primitive and String properties must be declared as 'const', node name = 'b'")
    }

    private fun compile(vararg srcs: SourceFile): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            sources = srcs.toList()
            symbolProcessorProviders = listOf(RikochetSymbolProcessorProvider())
        }.compile()
    }
}
