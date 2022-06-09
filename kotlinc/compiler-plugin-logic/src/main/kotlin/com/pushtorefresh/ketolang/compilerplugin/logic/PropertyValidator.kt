package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.kotlinFqName

fun validateProperty(
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl
): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    val parent = property.parent

    if (parent.kotlinFqName.isRoot) {
        errors += KetolangValidationError("Ketolang error: property must be declared in a named package!", property)
    }

    errors += when {
        parent is IrClass -> {
            when {
                parent.isObject -> validateObjectProperty(moduleFragment, property)
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
        property.isTopLevel -> validateTopLevelProperty(property)
        else -> listOf(
            KetolangValidationError(
                "Ketolang error: property looks suspicious! Perhaps Ketolang needs an update to validate it",
                property
            )
        )
    }

    return errors
}

private fun validateTopLevelProperty(
    property: IrPropertyImpl,
): List<KetolangValidationError> {
    return listOf(KetolangValidationError(
        "Ketolang error: top-level properties are not allowed! Use object class to declare them",
        property
    ))
}

private fun validateObjectProperty(
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
        (type?.isDataClass() == true) -> emptyList()
        (type?.isSomeCollection(moduleFragment) == true) -> {
            if (type.isImmutableCollection()) {
                emptyList()
            } else {
                listOf(
                    KetolangValidationError(
                        "Ketolang error: mutable collection properties are not allowed!",
                        property
                    )
                )
            }
        }

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

    val initializer = property.backingField?.initializer

    if (initializer != null) {
        errors += validateStatement(initializer.expression, moduleFragment)
    }

    return errors
}

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
                || type?.isDataClass() == true
                ) -> emptyList()

        (type?.isSomeCollection(moduleFragment) == true) -> {
            if (type.isImmutableCollection()) {
                emptyList()
            } else {
                listOf(
                    KetolangValidationError(
                        "Ketolang error: mutable collection properties are not allowed!",
                        property
                    )
                )
            }
        }

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
