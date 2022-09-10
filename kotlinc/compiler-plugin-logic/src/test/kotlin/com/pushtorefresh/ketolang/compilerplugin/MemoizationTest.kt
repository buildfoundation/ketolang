package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class MemoizationTest {

    @Test
    fun `ok`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package com.pushtorefresh.ketolang.sample

            val cache: MutableMap<Any, String> = mutableMapOf()

            fun f1(b: Int): String {
                return listOf(b, b).toString()
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }.forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `ok2`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package com.pushtorefresh.ketolang.sample

            val cache: MutableMap<Any, String> = mutableMapOf()

            fun f1(b: Int): String {
                return b.toString()
            }
    
            fun f2(b: Int): String {
                val cacheKey = listOf(b, b)
                val cached = cache.get(cacheKey)
                if (cached != null) {
                    return cached
                }
                val new = b.toString()
                cache[cacheKey] = new
                return new
            }

            fun b(s: String): String {
                val a = f1(1)
                val b = if (s.isEmpty()) f1(1) else f1(2)
                return a + b
            }

            // fib(n) = fib(n - 1) + fib(n - 2)
            fun naiveFibonacci(n: Int): Int {
                if (n == 0 || n == 1) {
                    return n
                } else {
                    return naiveFibonacci(n - 1) + naiveFibonacci(n - 2)
                }
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }.forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `single parameter function`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `single parameter function as expression`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int): String = b.toString()
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `naive fibonacci`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package com.pushtorefresh.ketolang.sample

            // fib(n) = fib(n - 1) + fib(n - 2)
            fun naiveFibonacci(n: Int): Int {
                if (n == 0 || n == 1) {
                    return n
                } else {
                    return naiveFibonacci(n - 1) + naiveFibonacci(n - 2)
                }
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
