package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isEnumEntry
import org.jetbrains.kotlin.ir.util.isInterface

fun validateClass(clazz: IrClassImpl): KetolangValidationError? {
    if (clazz.isInterface || clazz.modality == Modality.ABSTRACT) {
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
