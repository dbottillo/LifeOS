object Versions {
    const val gradle = "4.0.1"
    const val androidGradlePlugin = "7.3.1" // update also build.gradle.kts in buildSrc

    const val kotlin = "1.7.22" // update also build.gradle.kts in buildSrc
    const val coroutines = "1.6.4"

    object AndroidX {
        const val core = "1.9.0"
        const val cardview = "1.0.0"
        const val recyclerview = "1.2.1"
        const val compat = "1.5.1"
        const val preference = "1.2.0"
        const val navigation = "2.5.3"
        const val lifecycle = "2.5.1"
        const val lifecycleExtensions = "2.2.0"
        const val dataStore = "1.0.0"
    }

    const val material = "1.7.0"
    const val dagger = "2.44.2" // update also build.gradle.kts in buildSrc
    const val hilt = "1.0.0"
    const val leakCanary = "2.10"
    const val workManager = "2.7.1"

    const val constraint_layout = "2.1.4"

    object Retrofit {
        const val core = "2.9.0"
        const val moshi = "2.9.0"
    }
    const val moshi = "1.14.0"

    object OkHttp {
        const val core = "4.10.0"
        const val logging = "4.10.0"
    }

    // testing
    const val espresso = "3.2.0"
    const val mockito = "4.9.0"
    const val mockito_kotlin = "4.1.0"
    const val mockito_android = "3.8.0"
    const val junit = "5.9.1"
    const val truth = "1.1.3"
    const val lint = "30.3.1"
}
