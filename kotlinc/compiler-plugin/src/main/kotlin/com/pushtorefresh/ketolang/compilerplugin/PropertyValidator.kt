package com.pushtorefresh.ketolang.compilerplugin

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
): KetolangValidationError? {
    val parent = property.parent
    return when {
        parent is IrClass -> {
            when {
                parent.isData -> validateDataClassProperty(moduleFragment, property)
                parent.isEnumClass -> validateEnumProperty(moduleFragment, property)
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

    if (type.classOrNull?.descriptor?.isData == true) {
        return null
    }

    // TODO fix isCollection to actually check if is assignable from
    if (type.isSomeCollection(moduleFragment)) {
        if (type.isImmutableCollection(moduleFragment)) {
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

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun validateDataClassProperty(
    moduleFragment: IrModuleFragment,
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

    if (type.isPrimitiveType() || type.isString() || type.classOrNull?.descriptor?.isData == true) {
        return null
    }

    if (type.isSomeCollection(moduleFragment)) {
        if (type.isImmutableCollection(moduleFragment)) {
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
    moduleFragment: IrModuleFragment,
    property: IrPropertyImpl
): KetolangValidationError? {
    if (property.backingField == null && (property.name.asString() == "name" || property.name.asString() == "ordinal")) {
        return null
    } else {
        return validateDataClassProperty(moduleFragment, property)
    }
}
