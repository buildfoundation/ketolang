package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RegularClassesTest {

    @Test
    fun `class is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            class C {
                
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: regular classes are not allowed, only data classes and enums are allowed!, node name = 'C'"
        )
    }
}
