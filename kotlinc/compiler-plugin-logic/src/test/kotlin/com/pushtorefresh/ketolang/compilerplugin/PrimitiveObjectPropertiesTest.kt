package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PrimitiveObjectPropertiesTest {

    @Test
    fun `var Int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Int = 1
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Int is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Int = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Int is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Int = 1 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Long is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Long = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Long is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Long = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Long is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Long = 1 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Short is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Short = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Short is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Short = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Short is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Short = 1 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Byte is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Byte = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Byte is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Byte = 1 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Byte is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Byte = 1 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Double is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Double = 1.0 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Double is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Double = 1.0 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Double is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Double = 1.0 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var Float is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: Float = 1.0f
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val Float is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: Float = 1.0f
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val Float is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: Float = 1.0f 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `var String is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                var b: String = "abc" 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `val String is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                val b: String = "abc" 
            }"""
        )

        val result = compile(aKt)

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: primitive and String properties must be declared as 'const', node name = 'b'"
        )
    }

    @Test
    fun `const val String is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            object A {
                const val b: String = "abc" 
            }"""
        )

        val result = compile(aKt)
        assertEquals(ExitCode.OK, result.exitCode)
    }
}

