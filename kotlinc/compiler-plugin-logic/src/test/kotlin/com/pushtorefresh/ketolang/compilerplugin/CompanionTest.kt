package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CompanionTest {

    @Test
    fun `data class with companion is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            data class D(val i: Int) {
                companion object {
                    const val s = "abc" 
                }
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: companion objects are not allowed! " +
                    "Please use top-level object classes or top-level properties, node name = 'Companion'"
        )
    }
}
