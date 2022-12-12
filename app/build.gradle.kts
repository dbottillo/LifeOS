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
    implementation(project(":domain:domain_ui"))
    implementation(project(":feature_about:about_ui"))
    core()
    ui()
    di()
    network()
    debug()
    workManager()

    test()

    /*implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation "androidx.appcompat:appcompat:1.0.2"
    implementation "androidx.core:core-ktx:1.0.2"
    implementation "com.google.android.material:material:1.0.0"
    implementation "androidx.constraintlayout:constraintlayout:1.1.3"
    implementation "androidx.navigation:navigation-fragment-ktx:2.0.0"
    implementation "androidx.navigation:navigation-ui-ktx:2.0.0"
    testImplementation "junit:junit:4.12"
    androidTestImplementation "androidx.test.ext:junit:1.1.1"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.2.0"*/
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
