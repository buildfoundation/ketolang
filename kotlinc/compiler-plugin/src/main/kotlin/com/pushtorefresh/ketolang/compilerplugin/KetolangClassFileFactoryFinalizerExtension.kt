package com.pushtorefresh.ketolang.compilerplugin

import org.jetbrains.kotlin.codegen.ClassFileFactory
import org.jetbrains.kotlin.codegen.extensions.ClassFileFactoryFinalizerExtension

class KetolangClassFileFactoryFinalizerExtension : ClassFileFactoryFinalizerExtension {
    override fun finalizeClassFactory(factory: ClassFileFactory) {
        factory.toString()
    }
}
