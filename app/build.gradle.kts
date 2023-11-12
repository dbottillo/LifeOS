import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.dbottillo.notionalert"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"
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
        compose = true
    }

    buildTypes {
        val notionKey = gradleLocalProperties(rootDir).getProperty("notion_key")
        val pocketConsumerKey = gradleLocalProperties(rootDir).getProperty("pocket_consumer_key")
        getByName("debug") {
            buildConfigField("String", "NOTION_KEY", notionKey)
            buildConfigField("String", "POCKET_CONSUMER_KEY", pocketConsumerKey)
            matchingFallbacks.add("release")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("string", "NOTION_KEY", notionKey)
            buildConfigField("String", "POCKET_CONSUMER_KEY", pocketConsumerKey)
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

    namespace = "com.dbottillo.notionalert"

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    implementation(libs.bundles.ui)
    implementation(libs.bundles.network)
    kapt(libs.moshi.codegen)
    implementation(libs.bundles.old.ui)
    implementation(libs.bundles.work.manager)
    implementation(libs.bundles.hilt)
    implementation(libs.bundles.datastore)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose.ui)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.hilt.compiler)
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
