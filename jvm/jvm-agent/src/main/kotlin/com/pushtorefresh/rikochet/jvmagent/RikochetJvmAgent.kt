package com.pushtorefresh.rikochet.jvmagent

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy
import net.bytebuddy.agent.builder.AgentBuilder.TypeStrategy
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.StubMethod
import net.bytebuddy.matcher.ElementMatchers.any
import net.bytebuddy.matcher.ElementMatchers.nameStartsWith
import net.bytebuddy.matcher.ElementMatchers.named
import java.lang.instrument.Instrumentation
import kotlin.system.exitProcess

class RikochetJvmAgent {
    companion object {
        @JvmStatic
        fun premain(@Suppress("UNUSED_PARAMETER") agentArgs: String?, instrumentation: Instrumentation) {
            AgentBuilder.Default()
                .with(TypeStrategy.Default.REDEFINE)
                .with(RedefinitionStrategy.RETRANSFORMATION)
                .with(InitializationStrategy.NoOp.INSTANCE)
                .ignore(
                    nameStartsWith<TypeDescription?>("net.bytebuddy.")
                        .or(nameStartsWith("java.net."))
                        .or(nameStartsWith("java.util."))
                        .or(nameStartsWith("java.nio."))
                        .or(nameStartsWith("java.security."))
                        .or(nameStartsWith("jdk."))
                        .or(nameStartsWith("sun."))
                        .or(nameStartsWith("com.sun."))
                        .or(nameStartsWith("com.intellij."))
                        .or(nameStartsWith("kotlin."))
                )
                .type(any())
                .transform(AgentBuilder.Transformer { builder, typeDescription, classLoader, _ ->
                    when (val canonicalName = typeDescription.canonicalName ?: "") {
                        "java.io.FileOutputStream" -> builder.method(named("write")).intercept(StubMethod.INSTANCE)
                        "java.io.File" -> builder.method(named("open")).intercept(StubMethod.INSTANCE)
                        "java.lang.System" -> builder.method(named("currentTimeMillis")).intercept(StubMethod.INSTANCE)
                        else -> {
                            // Skip types from rt.jar, null means it's a bootstrap classloader.
                            if (classLoader != null) {
                                val badStaticFields = typeDescription
                                    .declaredFields
                                    .filter { it.isStatic }
                                    .filter {
                                        when {
                                            it.type.isPrimitive && it.isFinal -> false
                                            it.type.isArray -> true
                                            // TODO replace with proper Kotlin "companion object" check.
                                            it.name == "Companion" -> false
                                            it.descriptor == "Ljava/lang/String;" && it.isFinal -> false
                                            else -> true
                                        }
                                    }
                                    .joinToString { "$canonicalName.${it.name} of type ${it.type}" }

                                if (badStaticFields.isNotEmpty()) {
                                    System.err.println("Dynamic static fields are not allowed, but were found: $badStaticFields.\nStatic field can only be a constant of primitive type or a String.")
                                    exitProcess(2)
                                }
                            }

                            builder
                        }
                    }
                })
                .installOn(instrumentation)
        }
    }
}
