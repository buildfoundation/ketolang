plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:${deps.byte_buddy}")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Premain-class" to "com.pushtorefresh.ketolang.jvmagent.ketolangJvmAgent")
        attributes("Can-Redefine-Classes" to true)
        attributes("Can-Retransform-Classes" to true)
    }
}

