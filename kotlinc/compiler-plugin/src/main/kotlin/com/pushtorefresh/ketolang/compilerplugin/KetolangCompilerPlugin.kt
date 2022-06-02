package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
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
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        //ClassFileFactoryFinalizerExtension.registerExtension(project, KetolangClassFileFactoryFinalizerExtension())
        IrGenerationExtension.registerExtension(project, KetolangIrGenerationExtension(messageCollector))
    }
}
