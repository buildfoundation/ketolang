package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class Test {

    @Test
    fun `suspend function is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            suspend fun f(b: Int): String {
                return b.toString()
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "ketolang error: suspend functions are not allowed!, node name = 'f'"
        )
    }
}
