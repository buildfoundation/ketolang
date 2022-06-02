package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.ir.allParametersCount
import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.isReal
import org.jetbrains.kotlin.ir.util.statements

fun validateFunction(moduleFragment: IrModuleFragment, function: IrFunctionImpl): KetolangValidationError? {
    return when {
        function.parent is IrClassImpl -> validateClassFunction(function)
        function.isTopLevel -> validateTopLevelFunction(moduleFragment, function)
        else -> KetolangValidationError(
            "Ketolang error: function looks suspicious! Perhaps Ketolang needs an update to validate it",
            function
        )
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun validateClassFunction(function: IrFunctionImpl): KetolangValidationError? {
    if (function.descriptor is IrConstructor) {
        // Do we need additional constructor validation?
        return null
    } else if (!function.isReal) {
        // Auto-generated functions such as "clone", "finalize" and such.
        // If User implements then, then they're "real".
        return null
    } else if (function.origin is IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER) {
        return null
    } else if (function.origin is IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER) {
        return null
    } else {
        return KetolangValidationError(
            "Ketolang error: functions in classes are not allowed!",
            function
        )
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun validateTopLevelFunction(
    moduleFragment: IrModuleFragment,
    function: IrFunctionImpl
): KetolangValidationError? {
    val statementValidationError = function.body?.statements?.map { validateStatement(it) }
        ?.filterNotNull()?.firstOrNull()

    if (statementValidationError != null) {
        return statementValidationError
    }

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

    if (returnType.isSomeCollection(moduleFragment)) {
        if (returnType.isImmutableCollection(moduleFragment)) {
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
            .all {
                it.isPrimitiveType()
                        || it.isString()
                        || it.classOrNull?.descriptor?.isData == true
                        || it.isImmutableCollection(moduleFragment)
            }) {
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
