package com.pushtorefresh.ketolang.compilerplugin.logic

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrIfThenElseImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl

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

    "java.lang",
    "java.io",
    "java.nio",
    "java.net",
    "java.security",
)

fun validateStatement(statement: IrStatement, moduleFragment: IrModuleFragment): List<KetolangValidationError> {
    val errors = mutableListOf<KetolangValidationError>()

    when (statement) {
        is IrTypeOperatorCallImpl -> {
            errors += validateIrTypeOperatorCallImpl(statement, moduleFragment)
            errors += validateStatement(statement.argument, moduleFragment)
        }

        is IrCallImpl -> {
            val packageFqn = statement.symbol.signature?.packageFqName()?.asString()
            if (prohibitedPackages.contains(packageFqn)) {
                errors += KetolangValidationError("Ketolang error: call to prohibited package $packageFqn!", statement)
            }

            val dispatchReceiver = statement.dispatchReceiver

            if (dispatchReceiver != null) {
                errors += validateStatement(dispatchReceiver, moduleFragment)
            }
        }

        is IrConstructorCallImpl -> {
            val packageFqn = statement.symbol.signature?.packageFqName()?.asString()
            if (prohibitedPackages.contains(packageFqn)) {
                errors += KetolangValidationError(
                    "Ketolang error: call to prohibited package $packageFqn!",
                    statement
                )
            }
        }

        is IrVariableImpl -> {
            val initializer = statement.initializer

            if (initializer != null) {
                errors += validateStatement(initializer, moduleFragment)
            }
        }

        is IrReturnImpl -> {
            errors += validateStatement(statement.value, moduleFragment)
        }

        is IrWhenImpl -> {
            errors += statement.branches.flatMap { validateStatement(it.condition, moduleFragment) }
        }

        is IrBlockImpl -> {
            errors += statement.statements.flatMap { validateStatement(it, moduleFragment) }
        }

        is IrIfThenElseImpl -> {
            errors += statement.branches.flatMap {
                validateStatement(it.condition, moduleFragment) + validateStatement(it.result, moduleFragment)
            }
        }

        is IrThrowImpl -> {
            errors += KetolangValidationError(
                "Ketolang error: using 'throw' is not allowed, use 'error(\"text\")'!",
                statement
            )
        }

        is IrTryImpl -> {
            errors += KetolangValidationError(
                "Ketolang error: using 'try-catch' is not allowed, all exceptions are fatal!",
                statement
            )
        }
    }

    return errors
}

private fun validateIrTypeOperatorCallImpl(
    call: IrTypeOperatorCallImpl,
    moduleFragment: IrModuleFragment
): List<KetolangValidationError> {
    if (call.operator == IrTypeOperator.CAST || call.operator == IrTypeOperator.SAFE_CAST) {
        return listOf(KetolangValidationError("Ketolang error: explicit casting is prohibited!", call))
    }

    val errors = mutableListOf<KetolangValidationError>()

    if (call.operator == IrTypeOperator.INSTANCEOF) {
        if (call.typeOperand.isSomeCollection(moduleFragment) && !call.typeOperand.isImmutableCollection()) {
            errors += KetolangValidationError(
                "Ketolang error: type check for mutable collection types is not allowed!",
                call
            )
        }
    }

    return errors
}
