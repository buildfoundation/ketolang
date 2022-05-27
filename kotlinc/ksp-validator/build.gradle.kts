plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${deps.kotlin_symbol_processing}")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:${deps.kotlin_test}")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:${deps.kotlin_compile_testing}")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:${deps.kotlin_compile_testing}")
}
