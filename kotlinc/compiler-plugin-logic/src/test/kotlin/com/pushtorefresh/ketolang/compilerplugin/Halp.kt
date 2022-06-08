package com.pushtorefresh.ketolang.compilerplugin

import com.pushtorefresh.ketolang.compilerplugin.logic.KetolangIrGenerationExtension
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

fun compile(vararg srcs: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = srcs.toList()
        compilerPlugins = listOf(KetolangTestComponentRegistrar())
    }.compile()
}

class KetolangTestComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE]
        IrGenerationExtension.registerExtension(project, KetolangIrGenerationExtension(messageCollector))
    }
}
