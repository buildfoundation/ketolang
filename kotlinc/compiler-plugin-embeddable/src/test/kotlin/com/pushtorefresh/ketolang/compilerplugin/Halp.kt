package com.pushtorefresh.ketolang.compilerplugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile

fun compile(vararg srcs: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = srcs.toList()
        compilerPlugins = listOf(KetolangComponentRegistrar())
    }.compile()
}
