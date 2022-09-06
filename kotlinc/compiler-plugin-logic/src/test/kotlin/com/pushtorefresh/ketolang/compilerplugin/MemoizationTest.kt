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
            package p

            //val x: MutableMap<Any, Any> = java.util.concurrent.ConcurrentHashMap<Any, Any>()

            fun f(b: Int): String {
                return b.toString()
            }

            fun b(s: String): String {
                val a = f(1)
                val b = if (s.isEmpty()) f(1) else f(2)
                return a + b
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }.forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
