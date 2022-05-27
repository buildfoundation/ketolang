package com.pushtorefresh.rikochet.kotlinsymbolprocessor

import com.pushtorefresh.rikochet.kotlincsymbolprocessor.RikochetSymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders

fun compile(vararg srcs: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = srcs.toList()
        symbolProcessorProviders = listOf(RikochetSymbolProcessorProvider())
    }.compile()
}
