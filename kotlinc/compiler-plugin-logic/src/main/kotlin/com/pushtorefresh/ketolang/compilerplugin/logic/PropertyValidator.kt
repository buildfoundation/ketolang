package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.isEnumClass

fun validateProperty(
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl
): List<KetolangValidationError> {
    val parent = property.parent
    return when {
        parent is IrClass -> {
            when {
                parent.isData -> validateDataClassProperty(moduleFragment, property)
                parent.isEnumClass -> validateEnumProperty(moduleFragment, property)
                else -> listOf(
                    KetolangValidationError(
                        "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                        property
                    )
                )
            }
        }

        property.isTopLevel -> validateTopLevelProperty(moduleFragment, property)
        else -> listOf(
            KetolangValidationError(
                "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                property
            )
        )
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun validateTopLevelProperty(
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl,
): List<KetolangValidationError> {
    if (property.isConst) {
        return emptyList()
    }

    val errors = mutableListOf<KetolangValidationError>()

    val type = property.backingField?.type

    if (type?.isPrimitiveType() == true || type?.isString() == true) {
        errors += KetolangValidationError(
            "Ketolang error: primitive and String properties must be declared as 'const'",
            property
        )
    }

    if (property.isLateinit) {
        errors += KetolangValidationError("Ketolang error: lateinit properties are not allowed!", property)
    }

    if (property.isVar) {
        errors += KetolangValidationError(
            "Ketolang error: mutable properties are not allowed!",
            property
        )
    }

    errors += when {
        (type?.classOrNull?.descriptor?.isData == true) -> emptyList()
        (type?.isSomeCollection(moduleFragment) == true && !type?.isImmutableCollection(moduleFragment)) -> listOf(
            KetolangValidationError(
                "Ketolang error: mutable collection properties are not allowed!",
                property
            )
        )

        (type?.isArray() == true) -> listOf(
            KetolangValidationError(
                "Ketolang error: top-level array properties are not allowed because arrays are mutable",
                property
            )
        )

        (property.isDelegated) -> listOf(
            KetolangValidationError(
                "Ketolang error: delegated properties are not allowed!",
                property
            )
        )

        else -> listOf(
            KetolangValidationError(
                "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                property
            )
        )
    }


    return errors
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun validateDataClassProperty(
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl,
): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    if (property.isLateinit) {
        errors += KetolangValidationError("Ketolang error: lateinit properties are not allowed!", property)
    }

    if (property.isVar) {
        errors += KetolangValidationError(
            "Ketolang error: mutable properties are not allowed!",
            property
        )
    }

    val type = property.backingField?.type

    errors += when {
        (type?.isPrimitiveType() == true
                || type?.isString() == true
                || type?.classOrNull?.descriptor?.isData == true
                ) -> emptyList()

        (type?.isSomeCollection(moduleFragment) == true && !type.isImmutableCollection(moduleFragment)) -> listOf(
            KetolangValidationError(
                "Ketolang error: mutable collection properties are not allowed!",
                property
            )
        )

        type?.isArray() == true -> listOf(
            KetolangValidationError(
                "Ketolang error: array properties are not allowed because arrays are mutable",
                property
            )
        )

        else -> listOf(
            KetolangValidationError(
                "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                property
            )
        )
    }

    if (property.isDelegated) {
        errors += KetolangValidationError("Ketolang error: delegated properties are not allowed!", property)
    }

    return errors
}

private fun validateEnumProperty(
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl
): List<KetolangValidationError> {
    if (property.backingField == null
        && (
                property.name.asString() == "name"
                        || property.name.asString() == "ordinal"
                )
    ) {
        return emptyList()
    } else {
        return validateDataClassProperty(moduleFragment, property)
    }
}
