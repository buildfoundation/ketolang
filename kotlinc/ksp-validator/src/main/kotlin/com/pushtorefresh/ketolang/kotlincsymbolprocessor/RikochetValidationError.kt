package com.pushtorefresh.ketolang.kotlincsymbolprocessor

import com.google.devtools.ksp.symbol.KSNode

data class ketolangValidationError(val message: String, val node: KSNode)
