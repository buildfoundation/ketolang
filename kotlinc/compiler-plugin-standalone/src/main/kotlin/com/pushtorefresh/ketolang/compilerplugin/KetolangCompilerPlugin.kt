package com.pushtorefresh.ketolang.compilerplugin

import com.intellij.mock.MockProject
import com.pushtorefresh.ketolang.compilerplugin.logic.KetolangIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class KetolangCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "ketolang-compiler-plugin"
    override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}

class KetolangComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE]
        IrGenerationExtension.registerExtension(project, KetolangIrGenerationExtension(messageCollector))
    }
}
