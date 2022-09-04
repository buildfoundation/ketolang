package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.ir.allParametersCount
import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.isReal
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.SpecialNames

fun validateFunction(moduleFragment: IrModuleFragment, function: IrFunctionImpl): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    if (function.parent.kotlinFqName.isRoot) {
        errors += KetolangValidationError("Ketolang error: function must be declared in a named package!", function)
    }

    errors += when {
        function.parent is IrClassImpl -> validateClassFunction(function)
        function.isTopLevel -> validateTopLevelFunction(moduleFragment, function)
        else -> listOf(
            KetolangValidationError(
                "Ketolang error: function looks suspicious! Perhaps Ketolang needs an update to validate it",
                function
            )
        )
    }

    return errors
}

private fun validateClassFunction(function: IrFunctionImpl): List<KetolangValidationError> {
    if (function.name == SpecialNames.INIT) {
        // Do we need additional constructor validation?
        return emptyList()
    } else if (!function.isReal) {
        // Auto-generated functions such as "clone", "finalize" and such.
        // If User implements then, then they're "real".
        return emptyList()
    } else if (function.origin is IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER) {
        return emptyList()
    } else if (function.origin is IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER) {
        return emptyList()
    } else {
        return listOf(
            KetolangValidationError(
                "Ketolang error: functions in classes are not allowed!",
                function
            )
        )
    }
}

private fun validateTopLevelFunction(
    moduleFragment: IrModuleFragment,
    function: IrFunctionImpl
): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    if (function.isSuspend) {
        errors += KetolangValidationError("Ketolang error: suspend functions are not allowed!", function)
    }

    if (function.modality == Modality.OPEN) {
        errors += KetolangValidationError("Ketolang error: open functions are not allowed!", function)
    }

    if (function.overriddenSymbols.isNotEmpty()) {
        errors += KetolangValidationError(
            "Ketolang error: functions overriding other functions are not allowed!",
            function
        )
    }

    val returnType = function.returnType

    if (returnType.isUnit()) {
        errors += KetolangValidationError("Ketolang error: functions returning Unit are not allowed!", function)
    } else if (returnType.isAny()) {
        errors += KetolangValidationError("Ketolang error: functions returning Any are not allowed!", function)
    } else if (returnType.isSomeCollection(moduleFragment) && !returnType.isImmutableCollection()) {
        errors += KetolangValidationError(
            "Ketolang error: functions returning mutable collections are not allowed!",
            function
        )
    }

    if (function.allParametersCount == 0) {
        errors += KetolangValidationError(
            "Ketolang error: functions without parameters are not allowed!",
            function
        )
    }

    if (!function.allParameters.map { it.type }
            .all {
                it.isPrimitiveType()
                        || it.isString()
                        || it.isDataClass()
                        || it.isSealedClass()
                        || (
                        it.getClass()?.superTypes?.size == 1 && it.getClass()?.superTypes
                            ?.first()
                            ?.isSealedClass() == true
                        )
                        || it.isImmutableCollection()
            }) {
        errors += KetolangValidationError(
            // "Ketolang error: functions accepting \"mutable\" parameters are not allowed!
            // Allowed parameters: primitives, String, data classes, enums and collections of these.",
            "Ketolang error: functions accepting potentially mutable parameters are not allowed!",
            function
        )
    }

    errors += function.body?.statements?.flatMap { validateStatement(it, moduleFragment) } ?: emptyList()

    return errors
}
