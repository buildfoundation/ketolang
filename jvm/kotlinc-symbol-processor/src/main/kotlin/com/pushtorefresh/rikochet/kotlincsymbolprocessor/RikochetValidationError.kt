package com.pushtorefresh.rikochet.kotlincsymbolprocessor

import com.google.devtools.ksp.symbol.KSNode

data class RikochetValidationError(val message: String, val node: KSNode)
