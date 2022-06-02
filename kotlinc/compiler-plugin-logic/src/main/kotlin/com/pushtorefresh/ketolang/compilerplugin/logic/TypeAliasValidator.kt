package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.ir.declarations.impl.IrTypeAliasImpl

fun validateTypeAlias(typeAlias: IrTypeAliasImpl): List<KetolangValidationError> {
    return listOf(KetolangValidationError("Ketolang error: type-aliases are not allowed!", typeAlias))
}
