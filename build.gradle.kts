// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("unitTests") {
    group = "verification"
    description = "Runs all unit tests in the project."
    dependsOn(":app:testDebugUnitTest")
}

tasks.register("instrumentationTests") {
    group = "verification"
    description = "Runs all Android instrumented tests."
    dependsOn(":app:connectedDebugAndroidTest")
}
