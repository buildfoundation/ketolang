package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.ir.IrElement

data class KetolangValidationError(val message: String, val location: IrElement)
