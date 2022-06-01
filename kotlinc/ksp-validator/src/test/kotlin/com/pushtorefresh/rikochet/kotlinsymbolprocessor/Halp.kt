package com.pushtorefresh.ketolang.kotlinsymbolprocessor

import com.pushtorefresh.ketolang.kotlincsymbolprocessor.ketolangSymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders

fun compile(vararg srcs: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = srcs.toList()
        symbolProcessorProviders = listOf(ketolangSymbolProcessorProvider())
    }.compile()
}
