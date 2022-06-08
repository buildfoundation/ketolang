plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:${deps.kotlin}")
    implementation(project(":kotlinc:compiler-plugin-logic"))
}
