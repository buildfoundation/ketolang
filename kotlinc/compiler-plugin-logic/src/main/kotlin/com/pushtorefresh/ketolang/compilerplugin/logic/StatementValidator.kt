package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrIfThenElseImpl
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

fun validateStatement(statement: IrStatement): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    when (statement) {
        is IrTypeOperatorCallImpl -> {
            when (val argument = statement.argument) {
                is IrCallImpl -> {
                    val packageFqn = argument.symbol.signature?.packageFqName()?.asString()
                    if (prohibitedPackages.contains(packageFqn)) {
                        errors += KetolangValidationError(
                            "Ketolang error: call to prohibited package $packageFqn!",
                            statement
                        )
                    }
                }

                is IrConstructorCallImpl -> {
                    val packageFqn = argument.symbol.signature?.packageFqName()?.asString()
                    if (prohibitedPackages.contains(packageFqn)) {
                        errors += KetolangValidationError(
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
                errors += KetolangValidationError("Ketolang error: call to prohibited package $packageFqn!", statement)
            }
        }

        is IrVariableImpl -> {
            when (val initializer = statement.initializer) {
                is IrTypeOperatorCallImpl -> errors += validateIrTypeOperatorCallImpl(initializer)
            }
        }
    }

    return errors
}

fun validateExpression(expression: IrExpressionBody): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    when (val subExpression = expression.expression) {
        is IrTypeOperatorCallImpl -> errors += validateIrTypeOperatorCallImpl(subExpression)
        is IrIfThenElseImpl -> subExpression.branches.forEach {
            when (val condition = it.condition) {
                is IrCallImpl -> when (val dispatchReceiver = condition.dispatchReceiver) {
                    is IrTypeOperatorCallImpl -> errors += validateIrTypeOperatorCallImpl(dispatchReceiver)
                }
            }
        }
    }

    return errors
}

private fun validateIrTypeOperatorCallImpl(call: IrTypeOperatorCallImpl): List<KetolangValidationError> {
    if (call.operator == IrTypeOperator.CAST || call.operator == IrTypeOperator.SAFE_CAST) {
        return listOf(KetolangValidationError("Ketolang error: explicit casting is prohibited!", call))
    }

    return emptyList()
}
