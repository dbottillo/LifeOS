plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    kotlin("plugin.serialization") version "2.1.20"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
    extra.set("engBuild", project.findProperty("engBuild") ?: "true")
}

if (rootProject.extra.get("engBuild") == "true") {
    println("eng build pattern on!")
}

apply(plugin = "com.github.ben-manes.versions")

tasks {
    register("clean", Delete::class.java) {
        @Suppress("DEPRECATION")
        delete(rootProject.buildDir)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

val detektAll by tasks.registering(io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Custom Detekt task for all modules"
    parallel = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(files(projectDir))
    config.from(files(rootProject.files("config/detekt/detekt.yml")))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    baseline.fileValue(rootProject.file("config/detekt/baseline.xml"))
    reports {
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.html"))
        }
    }
}

val detektAllBaseline by tasks.registering(io.gitlab.arturbosch.detekt.DetektCreateBaselineTask::class) {
    description = "Custom Detekt task to build baseline for all modules"
    setSource(files(projectDir))
    baseline.fileValue(rootProject.file("config/detekt/baseline.xml"))
    config.from(files(rootProject.files("config/detekt/detekt.yml")))
    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**")
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
