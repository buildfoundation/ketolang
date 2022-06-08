package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeOrNull

fun IrType.isSomeCollection(moduleFragment: IrModuleFragment): Boolean {
    return isSubtypeOfClass(moduleFragment.irBuiltins.collectionClass)
            || isSubtypeOfClass(moduleFragment.irBuiltins.mapClass)
            || isSubtypeOfClass(moduleFragment.irBuiltins.mutableMapClass)
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrType.isImmutableCollection(): Boolean {
    val signature = classifierOrFail.signature

    val isImmutableCollection = signature == Signatures.LIST
            || signature == Signatures.SET
            || signature == Signatures.MAP

    if (!isImmutableCollection) {
        return false
    }

    (this as IrSimpleTypeImpl);

    return arguments.all { argument ->
        val type = argument.typeOrNull
        type?.isPrimitiveType() == true
                || type?.isString() == true
                || type?.classOrNull?.descriptor?.isData == true
    }
}
