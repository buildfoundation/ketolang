package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.isSealed
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isEnumEntry
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.packageFqName

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun validateClass(clazz: IrClassImpl, moduleFragment: IrModuleFragment): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    if (clazz.packageFqName?.isRoot == true) {
        errors += KetolangValidationError("Ketolang error: class must be declared in a named package!", clazz)
    }

    if (clazz.isInterface || clazz.modality == Modality.ABSTRACT) {
        errors += KetolangValidationError("Ketolang error: abstract classes and interfaces are not allowed!", clazz)
    }

    if (clazz.isData) {
        // Ok
    } else if (clazz.isEnumClass || clazz.isEnumEntry) {
        // Ok
    } else if (clazz.modality == Modality.SEALED) {
        // Ok
    } else if (clazz.isObject) {
        if (clazz.isCompanion) {
            errors += KetolangValidationError(
                "Ketolang error: companion objects are not allowed! " +
                        "Please use top-level object classes or top-level properties",
                clazz
            )
        }
    } else {
        errors += KetolangValidationError(
            "Ketolang error: regular classes are not allowed, only data classes and enums are allowed!",
            clazz
        )
    }

    if (clazz.superTypes.size != 1) {
        errors += KetolangValidationError(
            "Ketolang error: classes are only allowed to have 1 supertype!",
            clazz
        )
    } else {
        val superType = clazz.superTypes.first()

        if (superType.isAny()) {
            // Ok
        } else if (superType.isSubtypeOfClass(moduleFragment.irBuiltins.enumClass)) {
            // Ok
        } else if (superType.classifierOrNull?.descriptor?.isSealed() != true) {
            errors += KetolangValidationError(
                "Ketolang error: classes can only extend sealed classes!",
                clazz
            )
        }
    }

    return errors
}
