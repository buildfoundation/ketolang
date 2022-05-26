import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Declare, but do not apply plugins. This way we use same version across all modules.
plugins {
    kotlin("jvm").version(deps.kotlin).apply(false)
    id("com.google.devtools.ksp").version(deps.kotlin_symbol_processing).apply(false)
    id("io.gitlab.arturbosch.detekt").version(deps.detekt)
    id("com.github.ben-manes.versions").version(deps.gradle_versions_plugin)
    id("com.github.johnrengelman.shadow").version(deps.shadow_plugin)
}

val jvmTargetVersion = "17"
val testEventsToLog = setOf(TestLogEvent.STARTED, TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)

val topLevelProject = project

allprojects {
    repositories {
        mavenCentral()
    }

    afterEvaluate {
        tasks.filterIsInstance<JavaCompile>().forEach { compileJava ->
            compileJava.targetCompatibility = jvmTargetVersion
            compileJava.sourceCompatibility = jvmTargetVersion
        }

        tasks.filterIsInstance<KotlinCompile>().forEach { compileKotlin ->
            compileKotlin.kotlinOptions.allWarningsAsErrors = true
            compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
            compileKotlin.kotlinOptions.jvmTarget = jvmTargetVersion
        }

        // Apply Detekt to all modules with Kotlin plugin.
        if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            plugins.apply("io.gitlab.arturbosch.detekt")

            val moduleDetektBaseline = file("detekt-baseline.xml")

            detekt {
                parallel = true
                allRules = true
                config = files(relativePath(rootProject.rootDir) + "/gradle/plugins/detekt-config.yml")
                if (moduleDetektBaseline.exists()) {
                    baseline = moduleDetektBaseline
                }
                basePath = projectDir.path
                reports {
                    xml {
                        enabled = true
                    }
                    html {
                        enabled = false
                    }
                    txt {
                        enabled = false
                    }
                    // FYI: Sarif upload to GitHub requires GitHub Enterprise tear.
                    sarif {
                        enabled = false
                    }
                }
            }

            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                jvmTarget = jvmTargetVersion
            }
        }

        tasks.withType(Test::class) {
            useJUnitPlatform()
            project.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter:${deps.junit}")
            project.dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5:${deps.kotlin_test}")

            if (systemProperties["junit.jupiter.execution.parallel.enabled"] == null) {
                // Run separate test classes within a module in parallel.
                systemProperty("junit.jupiter.execution.parallel.enabled", "true")
                // Multiply CPU cores x2
                systemProperty("junit.jupiter.execution.parallel.config.dynamic.factor", "2")
                // Run separate tests in same class in parallel.
                systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
            }

            testLogging.events.addAll(testEventsToLog)
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            // Use same version for all Kotlin deps to avoid runtime errors.
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(deps.kotlin)
            }
        }
    }

    configurations.findByName("testImplementation")?.apply {
        // Remove JUnit4 from autocomplete.
        exclude("junit", "junit")
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
}
