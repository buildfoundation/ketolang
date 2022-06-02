package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CollectionTopLevelPropertiesTest {

    @Test
    fun `var List(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: List<Int> = listOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val List(Int) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: List<Int> = listOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val List(String) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: List<String> = listOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val List(Any) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: List<Any> = listOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val MutableList(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: MutableList<Int> = mutableListOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `var Set(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Set<Int> = setOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val Set(Int) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Set<Int> = setOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val Set(String) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Set<String> = setOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val Set(Any) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Set<Any> = setOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val MutableSet(Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: MutableSet<Int> = mutableSetOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `var Map(Int, Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            var b: Map<Int, Int> = mapOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val Map(Int, Int) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Map<Int, Int> = mapOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val Map(String, String) is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Map<String, String> = mapOf()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `val Map(Any, Any) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: Map<Any, Any> = mapOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }

    @Test
    fun `val MutableMap(Int, Int) is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            val b: MutableMap<Int, Int> = mutableMapOf()
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: mutable collection properties are not allowed!, node name = 'b'"
        )
    }
}
