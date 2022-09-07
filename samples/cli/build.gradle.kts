plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

tasks.filterIsInstance<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().forEach { compileKotlin ->
    compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}
