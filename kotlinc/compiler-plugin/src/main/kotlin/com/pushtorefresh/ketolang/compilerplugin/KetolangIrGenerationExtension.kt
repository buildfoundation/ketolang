package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.ir.allParametersCount
import org.jetbrains.kotlin.backend.common.ir.classIfConstructor
import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.getPublicSignature
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isCollection
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

class KetolangIrGenerationExtension(private val messageCollector: MessageCollector) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        val ketolangValidationErrors = moduleFragment
            .files
            .flatMap { file -> file.declarations.map { file to it } }
            .flatMap { (file, declaration) ->
                when (declaration) {
                    is IrFunctionImpl -> listOf(file to validateFunction(declaration))
                    else -> emptyList()
                }
            }
            .filter { (_, error) -> error != null }

        if (ketolangValidationErrors.isNotEmpty()) {
            ketolangValidationErrors.forEach { (file, error) ->
                messageCollector.report(CompilerMessageSeverity.ERROR,
                    "${error!!.message}, node name = '${error.location.printableName()}'",
                    file.locationOf(error.location)
                )
            }
            messageCollector.report(CompilerMessageSeverity.ERROR, "Ketolang validation errors were found, aborting compilation!")
        }
    }

    private fun validateFunction(function: IrFunctionImpl): KetolangValidationError? {
        return when {
            function.parent is IrClassImpl -> validateClassFunction(function)
            function.isTopLevel -> validateTopLevelFunction(function)
            else -> KetolangValidationError(
                "ketolang error: function looks suspicious! Perhaps ketolang needs an update to validate it",
                function
            )
        }
    }

    private fun validateClassFunction(function: IrFunctionImpl): KetolangValidationError? {
        if (function.classIfConstructor == function) {
            // TODO: validate constructors too.
            return null
        } else {
            return KetolangValidationError(
                "ketolang error: functions in classes are not allowed!",
                function
            )
        }
    }

    private fun validateTopLevelFunction(
        function: IrFunctionImpl
    ): KetolangValidationError? {
        if (function.isSuspend) {
            return KetolangValidationError("ketolang error: suspend functions are not allowed!", function)
        }

        val returnType = function.returnType

        if (returnType.isUnit()) {
            return KetolangValidationError("ketolang error: functions returning Unit are not allowed!", function)
        }

        if (returnType.isAny()) {
            return KetolangValidationError("ketolang error: functions returning Any are not allowed!", function)
        }

        if (returnType.isCollection()) {
            if (returnType.isImmutableCollection()) {
                return null
            } else {
                return KetolangValidationError(
                    "ketolang error: functions returning mutable collections are not allowed!",
                    function
                )
            }
        }

        if (function.allParametersCount == 0) {
            return KetolangValidationError(
                "ketolang error: functions without parameters are not allowed!",
                function
            )
        }

        if (function.allParameters.map { it.type }
                .all { it.isPrimitiveType() || it.isString() || it.isImmutableCollection() }) {
            return null
        } else {
            return KetolangValidationError(
                "ketolang error: functions accepting mutable parameters are not allowed!",
                function
            )
        }

        /*return ketolangValidationError(
            "ketolang error: function looks suspicious! Perhaps ketolang needs an update to validate it.",
            function
        )*/
    }

    private fun IrType.isImmutableCollection(): Boolean {
        val signature = classifierOrFail.signature

        val isImmutableCollection = signature == SIGNATURE_LIST
                || signature == SIGNATURE_SET
                || signature == SIGNATURE_MAP

        if (!isImmutableCollection) {
            return false
        }

        (this as IrSimpleTypeImpl);

        return arguments.all {
            val type = it.typeOrNull!!
            type.isPrimitiveType() || type.isString()
        }
    }

    private fun IrFile.locationOf(irElement: IrElement?): CompilerMessageSourceLocation {
        val sourceRangeInfo = fileEntry.getSourceRangeInfo(
            beginOffset = irElement?.startOffset ?: SYNTHETIC_OFFSET,
            endOffset = irElement?.endOffset ?: SYNTHETIC_OFFSET
        )
        return CompilerMessageLocationWithRange.create(
            path = sourceRangeInfo.filePath,
            lineStart = sourceRangeInfo.startLineNumber + 1,
            columnStart = sourceRangeInfo.startColumnNumber + 1,
            lineEnd = sourceRangeInfo.endLineNumber + 1,
            columnEnd = sourceRangeInfo.endColumnNumber + 1,
            lineContent = null
        )!!
    }

    private fun IrElement.printableName(): String? {
        return when (this) {
            is IrFunctionImpl -> name.asString()
            else -> "no printable name"
        }
    }

    companion object {
        val SIGNATURE_LIST = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "List")
        val SIGNATURE_SET = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Set")
        val SIGNATURE_MAP = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Map")
    }
}
