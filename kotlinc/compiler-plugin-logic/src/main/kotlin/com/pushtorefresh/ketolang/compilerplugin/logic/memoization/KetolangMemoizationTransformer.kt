@file:Suppress("unused", "UNUSED_VARIABLE")

package com.pushtorefresh.ketolang.compilerplugin.logic.memoization

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.ir.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isAccessor
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class KetolangMemoizationTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector
) : IrElementTransformerVoidWithContext() {

    private val irFactory: IrFactory = IrFactoryImpl

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private val javaUtilConcurrent: IrPackageFragment =
        createPackage(pluginContext.moduleDescriptor, "java.util.concurrent")

    private val javaUtilConcurrentHashMap: IrClassSymbol =
        createClass(javaUtilConcurrent, "ConcurrentHashMap", ClassKind.CLASS, Modality.OPEN)

    private val concurrentHashMapConstructor: IrConstructorSymbol = javaUtilConcurrentHashMap.owner.addConstructor().symbol

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration.isAccessor || declaration.isFakeOverride) {
            return super.visitFunctionNew(declaration)
        }

        // If Body is not Block -> turn it into a Block.
//        if (declaration.body !is IrBlockBody) {
//            declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol)
//                .irBlockBody {
//                    declaration.body!!.statements.forEach { +it }
//                }
//        }

        generateCacheProperty(declaration)

        /*DeclarationIrBuilder(pluginContext, declaration.symbol)
            .irBlockBody {
                +irCall()
            }*/

        return super.visitFunctionNew(declaration)
    }

    // Inserts "val cache_funname_arg_types: MutableMap<Any, Any?> = ConcurrentHashMap()" to the file where function is declared.
    private fun generateCacheProperty(declaration: IrFunction) {
        val paramFqns = declaration.allParameters
            .joinToString("_") { it.type.classFqName.toString().replace('.', 'D') }

        val fileParent = declaration.fileParent

        // Limit on JVM field names is 65k symbols https://stackoverflow.com/a/8782542/1562633
        // TODO check JS and Native and try to shorten the name while keeping it unique.
        val identifier = Name.identifier("memoized_${declaration.name.asString()}_$paramFqns")

        val property = declaration.factory.buildProperty {
            name = identifier
            isVar = false
            visibility = PRIVATE
        }.apply {
            parent = fileParent

            backingField = declaration.factory.buildField {
                name = identifier
                origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
                isFinal = true
                isStatic = true
                visibility = PRIVATE
                type = pluginContext.irBuiltIns.mutableMapClass.typeWith(
                    pluginContext.irBuiltIns.anyType,
                    pluginContext.irBuiltIns.anyType
                )
            }.apply {
                initializer = pluginContext.createIrBuilder(symbol).run {
                    irExprBody(
                        irCallConstructor(
                            concurrentHashMapConstructor, listOf(
                                pluginContext.irBuiltIns.anyType,
                                pluginContext.irBuiltIns.anyType
                            )
                        )
                    )
                }
            }
        }

        fileParent.declarations.add(property)
    }

    /**
     * @see org.jetbrains.kotlin.android.parcel.ir.AndroidSymbols.createPackage
     */
    private fun createPackage(moduleDescriptor: ModuleDescriptor, packageName: String): IrPackageFragment =
        IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
            moduleDescriptor,
            FqName(packageName)
        )

    /**
     * @see org.jetbrains.kotlin.android.parcel.ir.AndroidSymbols.createClass
     */
    private fun createClass(
        irPackage: IrPackageFragment,
        shortName: String,
        classKind: ClassKind,
        classModality: Modality
    ): IrClassSymbol = irFactory.buildClass {
        name = Name.identifier(shortName)
        kind = classKind
        modality = classModality
    }.apply {
        parent = irPackage
        createImplicitParameterDeclarationWithWrappedDescriptor()
    }.symbol

    private fun IrPluginContext.createIrBuilder(symbol: IrSymbol) =
        DeclarationIrBuilder(this, symbol, symbol.owner.startOffset, symbol.owner.endOffset)

}
