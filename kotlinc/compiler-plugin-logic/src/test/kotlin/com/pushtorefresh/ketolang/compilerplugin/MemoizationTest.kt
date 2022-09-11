package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class MemoizationTest {

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
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
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
    fun `single nullable parameter function`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int?): String = b?.toString() ?: "ok"
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `single parameter function with nullable result`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int): String? = if (b > 0) b.toString() else null
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
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

    @Test
    fun `tuple parameter function`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int, c: Int): String {
                return (b + c).toString()
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `triple parameter function`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int, c: Int, d: Int): String {
                return (b + c + d).toString()
            }
        """
        )

        val result = compile(aKt)
        // Copy files to IJ workspace to decompile and investigate, TODO remove
        result.compiledClassAndResourceFiles.filter { it.extension == "class" }
            .forEach { it.copyTo(File(it.name), overwrite = true) }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `quadruple parameter function`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f1(b: Int, c: Int, d: Int, e: Int): String {
                return (b + c + d + e).toString()
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
