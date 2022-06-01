package com.pushtorefresh.ketolang.kotlinsymbolprocessor

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PrimitiveTopLevelPropertiesTest {

    @Test
    fun `var Int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Int = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Int = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Int is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: Int = 1 
        """
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Long is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Long = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Long is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Long = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Long is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: Long = 1 
        """
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Short is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Short = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Short is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Short = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Short is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: Short = 1 
        """
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Byte is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Byte = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Byte is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Byte = 1 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Byte is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: Byte = 1 
        """
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var String is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: String = "abc" 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val String is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: String = "abc" 
        """
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val String is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            const val b: String = "abc" 
        """
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }
}
