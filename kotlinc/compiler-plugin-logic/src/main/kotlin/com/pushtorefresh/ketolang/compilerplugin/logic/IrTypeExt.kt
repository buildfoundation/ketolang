package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isClassType
import org.jetbrains.kotlin.ir.types.isNullablePrimitiveType
import org.jetbrains.kotlin.ir.types.isNullableString
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.isClass

fun IrType.isSealedClass(): Boolean {
    val clazz = getClass()
    return clazz?.isClass == true && clazz.modality == Modality.SEALED
}

fun IrType.isDataClass(): Boolean {
    return getClass()?.isData == true
}

fun IrType.isSomeCollection(moduleFragment: IrModuleFragment): Boolean {
    return isSubtypeOfClass(moduleFragment.irBuiltins.collectionClass)
            || isSubtypeOfClass(moduleFragment.irBuiltins.mapClass)
            || isSubtypeOfClass(moduleFragment.irBuiltins.mutableMapClass)
}

fun IrType.isImmutableCollection(): Boolean {
    val isImmutableCollection = isClassType(Signatures.LIST_FQN, false) || isClassType(Signatures.LIST_FQN, true)
            || isClassType(Signatures.SET_FQN, false) || isClassType(Signatures.SET_FQN, true)
            || isClassType(Signatures.MAP_FQN, false) || isClassType(Signatures.MAP_FQN, true)

    if (!isImmutableCollection) {
        return false
    }

    this as IrSimpleTypeImpl

    return arguments.all { argument ->
        val type = argument.typeOrNull
        type?.isPrimitiveType() == true
                || type?.isNullablePrimitiveType() == true
                || type?.isString() == true
                || type?.isNullableString() == true
                || type?.isDataClass() == true
    }
}
