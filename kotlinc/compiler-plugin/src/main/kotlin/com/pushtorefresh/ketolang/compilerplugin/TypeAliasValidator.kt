package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.ir.declarations.impl.IrTypeAliasImpl

fun validateTypeAlias(typeAlias: IrTypeAliasImpl): KetolangValidationError? {
    return KetolangValidationError("Ketolang error: type-aliases are not allowed!", typeAlias)
}
