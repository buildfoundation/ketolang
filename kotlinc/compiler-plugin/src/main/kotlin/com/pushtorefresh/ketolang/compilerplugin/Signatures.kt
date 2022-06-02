package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.types.getPublicSignature

object Signatures {
    val LIST = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "List")
    val SET = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Set")
    val MAP = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Map")
    val SIGNATURE_MUTABLE_MAP = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "MutableMap")
}
