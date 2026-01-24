import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.versions)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.dbottillo.lifeos"
        minSdk = 28
        targetSdk = 36
        versionCode = 33
        versionName = "2026-01-20"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
        viewBinding = false
    }

    signingConfigs {
        create("release") {
            // You need to specify either an absolute path or include the
            // keystore file in the same directory as the build.gradle file.
            storeFile = file("../lifeoskeystore.jks")
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("store_password")
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("key_alias")
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("key_password")
        }
    }

    buildTypes {
        val notionKey = gradleLocalProperties(rootDir, providers).getProperty("notion_key")
        getByName("debug") {
            buildConfigField("String", "NOTION_KEY", notionKey)
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "NOTION_KEY", notionKey)
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    lint {
        xmlReport = false
        checkDependencies = true
        lintConfig = file("$rootDir/config/lint/lint.xml")
    }

    namespace = "com.dbottillo.lifeos"
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val nonStable = listOf("ALPHA", "BETA", "RC", "DEV").any { version.uppercase().contains(it) }
    return nonStable
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.bundles.ui)
    implementation(libs.bundles.network)
    ksp(libs.moshi.codegen)
    implementation(libs.bundles.old.ui)
    implementation(libs.bundles.work.manager)
    implementation(libs.bundles.hilt)
    implementation(libs.bundles.datastore)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.bundles.androidx.glance)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)
    debugImplementation(libs.flipper)
    debugImplementation(libs.flipper.plugin.network)
    debugImplementation(libs.soloader)
    releaseImplementation(libs.flipper.noop)
    releaseImplementation(libs.flipper.plugins.noop)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
}
