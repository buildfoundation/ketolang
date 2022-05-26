package com.pushtorefresh.rikochet.kotlincsymbolprocessor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier

class RikochetSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private lateinit var MUTABLE_COLLECTION: KSClassDeclaration
    private lateinit var COLLECTION: KSClassDeclaration
    private lateinit var MAP: KSClassDeclaration

    override fun process(resolver: Resolver): List<KSAnnotated> {
        MUTABLE_COLLECTION = resolver.getClassDeclarationByName("kotlin.collections.MutableCollection")!!
        COLLECTION = resolver.getClassDeclarationByName("kotlin.collections.Collection")!!
        MAP = resolver.getClassDeclarationByName("kotlin.collections.Map")!!

        val rikochetValidationErrors = resolver
            .getAllFiles()
            .flatMap { file ->
                file
                    .declarations
                    .flatMap { declaration -> validateDeclaration(declaration, resolver) }
            }
            .filterNotNull()
            .toList()

        if (rikochetValidationErrors.isNotEmpty()) {
            rikochetValidationErrors.forEach { environment.logger.warn(it.message, it.node) }
            environment.logger.error("Rikochet validation errors were found, aborting compilation!")
        }

        return emptyList()
    }

    private fun validateDeclaration(declaration: KSDeclaration, resolver: Resolver): List<RikochetValidationError?> {
        return when (declaration) {
            is KSPropertyDeclaration -> listOf(validateProperty(declaration, resolver))
            is KSTypeAlias -> listOf(validateTypeAlias(declaration))
            is KSClassDeclaration -> declaration.declarations.flatMap { validateDeclaration(it, resolver) }.toList()
            else -> emptyList()
        }
    }

    private fun validateProperty(
        property: KSPropertyDeclaration,
        @Suppress("UNUSED_PARAMETER") resolver: Resolver
    ): RikochetValidationError? {
        // Checks are ordered in most likely frequent use-case order for better performance.

        if (property.modifiers.contains(Modifier.CONST)) {
            return null
        } else if (property.type.isPrimitive() || property.type.isString()) {
            return RikochetValidationError(
                "Rikochet error: primitive and String properties must be declared as 'const'",
                property
            )
        }

        if (property.isMutable) {
            return RikochetValidationError(
                "Rikochet error: mutable properties are not allowed!",
                property
            )
        }

        if (property.type.isCollection()) {
            if (property.type.isImmutableCollection()) {
                return null
            } else {
                return RikochetValidationError(
                    "Rikochet error: mutable collection properties are not allowed!",
                    property
                )
            }
        }

        if (property.type.isArray()) {
            return RikochetValidationError(
                "Rikochet error: top-level array properties are not allowed because arrays are mutable",
                property
            )
        }

        if (property.isDelegated()) {
            return RikochetValidationError("Rikochet error: delegated properties are not allowed!", property)
        }

        return RikochetValidationError(
            "Rikochet error: property looks suspicious! Perhaps Rikochet needs an update to validate it",
            property
        )
    }

    private fun validateTypeAlias(typeAlias: KSTypeAlias): RikochetValidationError? {
        return RikochetValidationError("Rikochet error: type-aliases are not allowed!", typeAlias)
    }

    private fun KSTypeReference.isPrimitive(): Boolean {
        val qualifiedName = this.resolve().declaration.qualifiedName?.asString()
        return qualifiedName == "kotlin.Int"
                || qualifiedName == "kotlin.Long"
                || qualifiedName == "kotlin.Short"
                || qualifiedName == "kotlin.Byte"
    }

    private fun KSTypeReference.isString(): Boolean {
        return this.resolve().declaration.qualifiedName?.asString() == "kotlin.String"
    }

    private fun KSTypeReference.isArray(): Boolean {
        return this.resolve().declaration.qualifiedName?.asString() == "kotlin.Array"
    }

    private fun KSTypeReference.isImmutableCollection(): Boolean {
        val resolved = this.resolve()
        val qualifiedName = resolved.declaration.qualifiedName?.asString()

        val isImmutableCollection = qualifiedName == "kotlin.collections.List"
                || qualifiedName == "kotlin.collections.Set"
                || qualifiedName == "kotlin.collections.Map"

        if (!isImmutableCollection) {
            return false
        }

        return resolved.arguments.all { it.type!!.isPrimitive() || it.type!!.isString() }
    }

    private fun KSTypeReference.isCollection(): Boolean {
        val resolved = this.resolve()
        return COLLECTION.asStarProjectedType().isAssignableFrom(resolved) || MAP.asStarProjectedType()
            .isAssignableFrom(resolved)
    }
}
