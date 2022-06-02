plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:${deps.kotlin}")
}
