package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LabelsTest {

    @Test
    fun `label is not allowed`() {
        val aKt = SourceFile.kotlin(
            "a.kt", """
            package p

            fun f(i: Int): String {
                var s = ""

                outerloop@ for (j in 1..(10+i)) {
                    s += j.toString()
                    for (k in 1..(10+j)) {
                        if (k % 2 == 0) {
                            continue@outerloop
                        }
                        s+=k.toString()
                    }
                }

                return s
            }
        """
        )

        val result = compile(aKt)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(
            result.messages,
            "Ketolang error: using labels is not allowed!, node name = 'no printable name'"
        )
    }
}
