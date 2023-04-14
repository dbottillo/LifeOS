import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.protobuf.gradle.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("common-dagger-precompiled")
    id("com.google.protobuf") version "0.8.19"
}

android {
    compileSdk = Config.Android.compileSdk
    buildToolsVersion = Config.Android.buildTools

    defaultConfig {
        applicationId = Config.Android.applicationId
        minSdk = Config.Android.minSDk
        targetSdk = Config.Android.targetSdk
        versionCode = Config.Android.versionCode
        versionName = Config.Android.versionName
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        xmlReport = false
        checkDependencies = true
        lintConfig = file("$rootDir/config/lint/lint.xml")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0:osx-x86_64"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    core()
    ui()
    di()
    network()
    debug()
    workManager()

    test()
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
