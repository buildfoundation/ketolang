@file:Suppress("unused", "UNUSED_VARIABLE", "UNUSED_PARAMETER")

package com.pushtorefresh.ketolang.compilerplugin.logic.memoization

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.ir.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.jvm.functionByName
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
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isAccessor
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray

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

    private val concurrentHashMapConstructor: IrConstructorSymbol =
        javaUtilConcurrentHashMap.owner.addConstructor().symbol

    private val mutableMapGetFunction = pluginContext.symbols.mutableMap.functionByName("get")
    private val mutableMapPutFunction = pluginContext.symbols.mutableMap.functionByName("put")

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private val listOfMultiArgFunction = pluginContext.irBuiltIns
        .findFunctions(Name.identifier("listOf"), "kotlin", "collections")
        .single { it.descriptor.valueParameters.firstOrNull() { it.type.isArrayOrNullableArray() } != null }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val function = declaration
        if (function.isAccessor || function.isFakeOverride) {
            return super.visitFunctionNew(function)
        }

        // If Body is not Block -> turn it into a Block.
//        if (declaration.body !is IrBlockBody) {
//            declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol)
//                .irBlockBody {
//                    declaration.body!!.statements.forEach { +it }
//                }
//        }

        val memoizedStorageProperty = generateMemoizedStorageProperty(function)
        function.fileParent.declarations.add(memoizedStorageProperty)

        val functionBody = function.body as IrBlockBody

        val memoizedKeyVariable = generateMemoizedKeyVariable(function)
        functionBody.statements.add(0, memoizedKeyVariable)

        val checkMemoizedStorageAndReturnValueStatements =
            generateCheckMemoizedStorageAndReturnStatements(function, memoizedStorageProperty, memoizedKeyVariable)
        functionBody.statements.addAll(1, checkMemoizedStorageAndReturnValueStatements)

        // TODO do not replace our own generated return!
        // TODO make sure we're diving deep into statements, use transforming visitor perhaps.
        functionBody.statements.replaceAll { statement ->
            when (statement) {
                is IrReturn -> generateReturnReplacementStatements(
                    function,
                    memoizedStorageProperty,
                    memoizedKeyVariable,
                    statement
                )

                else -> statement
            }
        }

        return super.visitFunctionNew(function)
    }

    private fun generateMemoizedStorageProperty(function: IrFunction): IrProperty {
        val paramFqns = function.allParameters
            .joinToString("_") { it.type.classFqName.toString().replace('.', 'D') }

        val fileParent = function.fileParent

        // Limit on JVM field names is 65k symbols https://stackoverflow.com/a/8782542/1562633
        // TODO check JS and Native and try to shorten the name while keeping it unique.
        val identifier = Name.identifier("memoized_${function.name.asString()}_$paramFqns")

        return function.factory.buildProperty {
            name = identifier
            visibility = PRIVATE
        }.apply {
            parent = fileParent

            backingField = function.factory.buildField {
                name = identifier
                origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
                isFinal = true
                isStatic = true
                visibility = PRIVATE
                type = pluginContext.irBuiltIns.mutableMapClass.typeWith(
                    pluginContext.irBuiltIns.anyType,
                    function.returnType
                )
            }.apply {
                initializer = pluginContext.createIrBuilder(symbol).run {
                    irExprBody(
                        irCallConstructor(
                            concurrentHashMapConstructor,
                            typeArguments = listOf(
                                pluginContext.irBuiltIns.anyType,
                                function.returnType
                            )
                        )
                    )
                }
            }
        }
    }

    // TODO generate specialized versions for function with 1 parameter
    //  so we don't have to allocate a List of keys to query memoized storage.
    private fun generateMemoizedKeyVariable(function: IrFunction): IrVariable {
        return pluginContext.createIrBuilder(function.symbol).run {
            irBlock {
                +buildVariable(
                    name = Name.identifier("ketolang_memoized_key"),
                    type = pluginContext.irBuiltIns.listClass.typeWith(pluginContext.irBuiltIns.anyType),
                    parent = function,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    origin = IrDeclarationOrigin.DEFINED
                ).apply {
                    initializer = irCall(listOfMultiArgFunction).apply {
                        val params = function.valueParameters.map { irGet(it) }
                        putValueArgument(0, irVararg(pluginContext.irBuiltIns.anyType, params))
                    }
                }
            }
        }.statements.single() as IrVariable
    }

    // See AndroidIrExtension.getCachedFindViewByIdFun
    private fun generateCheckMemoizedStorageAndReturnStatements(
        function: IrFunction,
        memoizedStorage: IrProperty,
        memoizedKey: IrVariable
    ): List<IrStatement> {
        return pluginContext.createIrBuilder(function.symbol).run {
            irBlock {
                val memoizedValue = irTemporary(
                    nameHint = "memoized_value",
                    irType = function.returnType.makeNullable(),
                    value = irCall(mutableMapGetFunction, pluginContext.irBuiltIns.anyNType).apply {
                        dispatchReceiver = irGetField(null, memoizedStorage.backingField!!)
                        putValueArgument(0, irGet(memoizedKey))
                    },
                )

                +irIfThen(irNotEquals(irGet(memoizedValue), irNull()), irReturn(irGet(memoizedValue)))
            }
        }.statements
    }

    private fun generateReturnReplacementStatements(
        function: IrFunction,
        memoizedStorage: IrProperty,
        memoizedKey: IrVariable,
        originalReturnStatement: IrReturn
    ): IrStatement {
        return pluginContext.createIrBuilder(function.symbol).run {
            irBlock {
                val originalReturnResult = irTemporary(
                    nameHint = "original_return_result",
                    value = originalReturnStatement.value
                )

                +irCall(mutableMapPutFunction, pluginContext.irBuiltIns.anyNType).apply {
                    dispatchReceiver = irGetField(null, memoizedStorage.backingField!!)
                    putValueArgument(0, irGet(memoizedKey))
                    putValueArgument(1, irGet(originalReturnResult))
                }

                +irReturn(irGet(originalReturnResult))
            }
        }
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
