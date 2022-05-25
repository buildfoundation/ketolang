import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

group = "com.pushtorefresh.rikochet.dummyapp"
version = "dev"

application {
    mainClass.set("com.pushtorefresh.rikochet.dummyapp.Main-${System.currentTimeMillis()}")
}

tasks.withType<KotlinCompile> {
    // Make sure JVM agent is not outdated.
    dependsOn(
        ":jvm:jvm-agent:shadowJar"
    )
}
