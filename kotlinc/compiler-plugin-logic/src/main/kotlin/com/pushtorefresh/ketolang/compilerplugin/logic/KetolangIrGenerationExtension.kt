package com.pushtorefresh.ketolang.compilerplugin.logic

import com.pushtorefresh.ketolang.compilerplugin.logic.memoization.KetolangMemoizationTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrTypeAliasImpl

class KetolangIrGenerationExtension(private val messageCollector: MessageCollector) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val ketolangValidationErrors = moduleFragment
            .files
            .flatMap { file -> file.declarations.map { file to it } }
            .flatMap { (file, declaration) -> validateDeclaration(moduleFragment, file, declaration) }
            .filter { (_, error) -> error != null }

        if (ketolangValidationErrors.isNotEmpty()) {
            ketolangValidationErrors.forEach { (file, error) ->
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "${error?.message}, node name = '${error?.location?.printableName()}'",
                    file.locationOf(error?.location)
                )
            }
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Ketolang validation errors were found, aborting compilation!"
            )
        }

        moduleFragment.transform(KetolangMemoizationTransformer(pluginContext, messageCollector), data = null)
    }

    private fun validateDeclaration(
        moduleFragment: IrModuleFragment,
        file: IrFile,
        declaration: IrDeclaration
    ): List<Pair<IrFile, KetolangValidationError?>> {
        return when (declaration) {
            is IrPropertyImpl -> validateProperty(moduleFragment, declaration)
            is IrFunctionImpl -> validateFunction(moduleFragment, declaration)
            is IrTypeAliasImpl -> validateTypeAlias(declaration)
            is IrClassImpl -> {
                validateClass(declaration, moduleFragment) + declaration
                    .declarations
                    .flatMap { validateDeclaration(moduleFragment, file, it).map { (_, error) -> error } }
                    .toList()
            }
            else -> emptyList()
        }.map { error -> file to error }
    }
}
