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
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrTypeAliasImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.getPublicSignature
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isCollection
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isEnumEntry
import org.jetbrains.kotlin.ir.util.isInterface

class KetolangIrGenerationExtension(private val messageCollector: MessageCollector) : IrGenerationExtension {

    private val SIGNATURE_LIST = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "List")
    private val SIGNATURE_SET = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Set")
    private val SIGNATURE_MAP = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Map")
    private val SIGNATURE_MUTABLE_MAP = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "MutableMap")

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
                    "${error!!.message}, node name = '${error.location.printableName()}'",
                    file.locationOf(error.location)
                )
            }
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Ketolang validation errors were found, aborting compilation!"
            )
        }
    }

    private fun validateDeclaration(
        moduleFragment: IrModuleFragment,
        file: IrFile,
        declaration: IrDeclaration
    ): List<Pair<IrFile, KetolangValidationError?>> {
        return when (declaration) {
            is IrPropertyImpl -> listOf(validateProperty(moduleFragment, declaration))
            is IrFunctionImpl -> listOf(validateFunction(moduleFragment, declaration))
            is IrTypeAliasImpl -> listOf(validateTypeAlias(declaration))
            is IrClassImpl -> {
                validateClass(declaration).let { error ->
                    if (error == null) {
                        declaration
                            .declarations
                            .flatMap { validateDeclaration(moduleFragment, file, it).map { (_, error) -> error } }
                            .toList()
                    } else {
                        listOf(error)
                    }
                }
            }

            else -> emptyList()
        }.map { error -> file to error }
    }

    private fun validateProperty(
        moduleFragment: IrModuleFragment,
        property: IrPropertyImpl
    ): KetolangValidationError? {
        val parent = property.parent
        return when {
            parent is IrClass -> {
                when {
                    parent.isData -> validateDataClassProperty(
                        property
                    )

                    parent.isEnumClass -> validateEnumProperty(property)
                    else -> KetolangValidationError(
                        "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                        property
                    )
                }
            }

            property.isTopLevel -> validateTopLevelProperty(moduleFragment, property)
            else -> KetolangValidationError(
                "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                property
            )
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun validateTopLevelProperty(
        moduleFragment: IrModuleFragment,
        property: IrPropertyImpl,
    ): KetolangValidationError? {
        val type by lazy(LazyThreadSafetyMode.NONE) { property.backingField!!.type }

        if (property.isConst) {
            return null
        } else if (type.isPrimitiveType() || type.isString()) {
            return KetolangValidationError(
                "Ketolang error: primitive and String properties must be declared as 'const'",
                property
            )
        }

        if (property.isLateinit) {
            return KetolangValidationError("Ketolang error: lateinit properties are not allowed!", property)
        }

        if (property.isVar) {
            return KetolangValidationError(
                "Ketolang error: mutable properties are not allowed!",
                property
            )
        }

        // TODO fix isCollection to actually check if is assignable from
        if (type.isSomeCollection(moduleFragment)) {
            if (type.isImmutableCollection()) {
                return null
            } else {
                return KetolangValidationError(
                    "Ketolang error: mutable collection properties are not allowed!",
                    property
                )
            }
        }

        if (type.isArray()) {
            return KetolangValidationError(
                "Ketolang error: top-level array properties are not allowed because arrays are mutable",
                property
            )
        }

        if (property.isDelegated) {
            return KetolangValidationError("Ketolang error: delegated properties are not allowed!", property)
        }

        return KetolangValidationError(
            "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
            property
        )
    }

    private fun validateDataClassProperty(
        property: IrPropertyImpl,
    ): KetolangValidationError? {
        val type by lazy(LazyThreadSafetyMode.NONE) { property.backingField!!.type }

        if (property.isLateinit) {
            return KetolangValidationError("Ketolang error: lateinit properties are not allowed!", property)
        }

        if (property.isVar) {
            return KetolangValidationError(
                "Ketolang error: mutable properties are not allowed!",
                property
            )
        }

        if (type.isPrimitiveType() || type.isString()) {
            return null
        }

        if (type.isCollection()) {
            if (type.isImmutableCollection()) {
                return null
            } else {
                return KetolangValidationError(
                    "Ketolang error: mutable collection properties are not allowed!",
                    property
                )
            }
        }

        if (type.isArray()) {
            return KetolangValidationError(
                "Ketolang error: array properties are not allowed because arrays are mutable",
                property
            )
        }

        if (property.isDelegated) {
            return KetolangValidationError("Ketolang error: delegated properties are not allowed!", property)
        }

        return KetolangValidationError(
            "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
            property
        )
    }

    private fun validateEnumProperty(
        property: IrPropertyImpl
    ): KetolangValidationError? {
        if (property.backingField == null && (property.name.asString() == "name" || property.name.asString() == "ordinal")) {
            return null
        } else {
            return validateDataClassProperty(property)
        }
    }

    private fun validateFunction(@Suppress("UNUSED_PARAMETER") moduleFragment: IrModuleFragment, function: IrFunctionImpl): KetolangValidationError? {
        return when {
            function.parent is IrClassImpl -> validateClassFunction(function)
            function.isTopLevel -> validateTopLevelFunction(function)
            else -> KetolangValidationError(
                "Ketolang error: function looks suspicious! Perhaps Ketolang needs an update to validate it",
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
                "Ketolang error: functions in classes are not allowed!",
                function
            )
        }
    }

    private fun validateTopLevelFunction(
        function: IrFunctionImpl
    ): KetolangValidationError? {
        if (function.isSuspend) {
            return KetolangValidationError("Ketolang error: suspend functions are not allowed!", function)
        }

        val returnType = function.returnType

        if (returnType.isUnit()) {
            return KetolangValidationError("Ketolang error: functions returning Unit are not allowed!", function)
        }

        if (returnType.isAny()) {
            return KetolangValidationError("Ketolang error: functions returning Any are not allowed!", function)
        }

        if (returnType.isCollection()) {
            if (returnType.isImmutableCollection()) {
                return null
            } else {
                return KetolangValidationError(
                    "Ketolang error: functions returning mutable collections are not allowed!",
                    function
                )
            }
        }

        if (function.allParametersCount == 0) {
            return KetolangValidationError(
                "Ketolang error: functions without parameters are not allowed!",
                function
            )
        }

        if (function.allParameters.map { it.type }
                .all { it.isPrimitiveType() || it.isString() || it.isImmutableCollection() }) {
            return null
        } else {
            return KetolangValidationError(
                "Ketolang error: functions accepting mutable parameters are not allowed!",
                function
            )
        }

        /*return ketolangValidationError(
            "Ketolang error: function looks suspicious! Perhaps Ketolang needs an update to validate it.",
            function
        )*/
    }

    private fun validateTypeAlias(typeAlias: IrTypeAliasImpl): KetolangValidationError? {
        return KetolangValidationError("Ketolang error: type-aliases are not allowed!", typeAlias)
    }

    private fun validateClass(clazz: IrClassImpl): KetolangValidationError? {
        if (clazz.isInterface) {
            return KetolangValidationError("Ketolang error: abstract classes and interfaces are not allowed!", clazz)
        }

        if (clazz.isData) {
            return null
        } else if (clazz.isEnumClass || clazz.isEnumEntry) {
            return null
        }

        return KetolangValidationError(
            "Ketolang error: regular classes are not allowed, only data classes and enums are allowed!",
            clazz
        )
    }

    private fun IrType.isSomeCollection(moduleFragment: IrModuleFragment): Boolean {
        return isSubtypeOfClass(moduleFragment.irBuiltins.collectionClass)
                || classifierOrFail.signature == SIGNATURE_MUTABLE_MAP
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
            is IrPropertyImpl -> name.asString()
            is IrFunctionImpl -> name.asString()
            else -> "no printable name"
        }
    }
}
