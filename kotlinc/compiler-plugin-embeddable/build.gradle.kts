plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:${deps.kotlin}")
    implementation(project(":kotlinc:compiler-plugin-logic"))
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:${deps.kotlin_test}")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:${deps.kotlin_compile_testing}")
}
