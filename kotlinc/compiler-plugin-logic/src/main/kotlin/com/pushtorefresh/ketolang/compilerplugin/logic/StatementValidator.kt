package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl

private val prohibitedPackages: Set<String> = setOf(
    "kotlin.annotation",
    "kotlin.concurrent",
    "kotlin.contracts",
    "kotlin.coroutines",
    "kotlin.experimental",
    "kotlin.internal",
    "kotlin.io",
    "kotlin.js",
    "kotlin.jvm",
    "kotlin.math",
    "kotlin.properties",
    "kotlin.random",
    "kotlin.ranges",
    "kotlin.reflect",
    "kotlin.system",
    "kotlin.time",

    "java.io",
    "java.nio",
    "java.net",
    "java.security",
)

fun validateStatement(statement: IrStatement): KetolangValidationError? {
    when (statement) {
        is IrTypeOperatorCallImpl -> {
            when (val argument = statement.argument) {
                is IrCallImpl -> {
                    val packageFqn = argument.symbol.signature?.packageFqName()?.asString()
                    if (prohibitedPackages.contains(packageFqn)) {
                        return KetolangValidationError(
                            "Ketolang error: call to prohibited package $packageFqn!",
                            statement
                        )
                    }
                }

                is IrConstructorCallImpl -> {
                    val packageFqn = argument.symbol.signature?.packageFqName()?.asString()
                    if (prohibitedPackages.contains(packageFqn)) {
                        return KetolangValidationError(
                            "Ketolang error: call to prohibited package $packageFqn!",
                            statement
                        )
                    }
                }
            }
        }

        is IrCallImpl -> {
            val packageFqn = statement.symbol.signature?.packageFqName()?.asString()
            if (prohibitedPackages.contains(packageFqn)) {
                return KetolangValidationError("Ketolang error: call to prohibited package $packageFqn!", statement)
            }
        }
    }

    return null
}
