package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class InterfacesTest {

    @Test
    fun `interface is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            interface I {
                
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: abstract classes and interfaces are not allowed!, node name = 'I'"
        )
    }

    @Test
    fun `abstract class is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            abstract class A {
                
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: abstract classes and interfaces are not allowed!, node name = 'A'"
        )
    }
}
