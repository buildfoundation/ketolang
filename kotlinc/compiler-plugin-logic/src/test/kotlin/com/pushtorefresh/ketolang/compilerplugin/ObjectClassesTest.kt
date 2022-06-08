package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectClassesTest {

    @Test
    fun `object class is allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            object MyStuff {
                const val s = "abc"
            }
        """
        )

        val result = compile(aKt)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
