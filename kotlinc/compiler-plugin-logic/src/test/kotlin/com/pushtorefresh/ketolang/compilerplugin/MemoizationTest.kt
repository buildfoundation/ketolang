package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class MemoizationTest {

    val NULL_INDICATOR = Any()

    val cache = mutableMapOf<String, Any?>()

    fun fCached(functionId: String, args: List<Any?>, compute: (List<Any?>) -> Any?): Any? {
        val cached = cache[functionId]
        return if (cached != null) {
            if (cached == NULL_INDICATOR) {
                null
            } else {
                cached
            }
        } else {
            val new = compute(args) ?: NULL_INDICATOR
            cache[functionId] = new
            new
        }
    }

    @Test
    fun `ok`() {
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
}
