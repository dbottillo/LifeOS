plugins {
    id("kotlin")
    id("common-precompiled")
}

dependencies {
    implementation(project(":core:core_data"))
    implementation(project(":domain:domain_data"))

    core()
    di()

    test()
    lintChecks(project(":lint-rules"))
}
