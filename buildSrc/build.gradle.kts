plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
}

dependencies {

    val kotlin = "1.8.10"
    val androidGradlePlugin = "7.4.2"
    val dagger = "2.45"

    implementation("com.android.tools.build:gradle:$androidGradlePlugin")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
    implementation("com.google.dagger:hilt-android-gradle-plugin:$dagger")

    /* Depend on the default Gradle API's since we want to build a custom plugin */
    implementation(gradleApi())
    implementation(localGroovy())
}
