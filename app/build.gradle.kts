import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.versions)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.dbottillo.lifeos"
        minSdk = 28
        targetSdk = 34
        versionCode = 9
        versionName = "0.1.0"
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

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        xmlReport = false
        checkDependencies = true
        lintConfig = file("$rootDir/config/lint/lint.xml")
    }

    namespace = "com.dbottillo.lifeos"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.bundles.ui)
    implementation(libs.bundles.network)
    kapt(libs.moshi.codegen)
    implementation(libs.bundles.old.ui)
    implementation(libs.bundles.work.manager)
    implementation(libs.bundles.hilt)
    implementation(libs.bundles.datastore)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.bundles.androidx.glance)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)
}

kapt {
    useBuildCache = true
    correctErrorTypes = true
}

task("devTest") {
    dependsOn("testDebugUnitTest")
}

task("stagingTest") {
    dependsOn("testDebugUnitTest")
}

task("prodTest") {
    dependsOn("testReleaseUnitTest")
}
