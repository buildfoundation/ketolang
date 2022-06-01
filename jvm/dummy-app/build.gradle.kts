import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "com.pushtorefresh.ketolang.dummyapp"
version = "dev"

application {
    mainClass.set("com.pushtorefresh.ketolang.dummyapp.Main-${System.currentTimeMillis()}")
}

tasks.withType<KotlinCompile> {
    // Make sure JVM agent is not outdated.
    dependsOn(
        ":jvm:jvm-agent:shadowJar"
    )
}

dependencies {
    ksp(project(":kotlinc:ksp-validator"))
}
