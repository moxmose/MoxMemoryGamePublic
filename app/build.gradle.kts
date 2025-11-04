plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.moxmemorygame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.moxmemorygame"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // Add JVM arguments for Robolectric/Mockito to work with modern Java
            all {
                it.jvmArgs("-Xmx4g", "-noverify")
            }
        }
    }
}

dependencies {
    // Import the Compose Bill of Materials (BOM)
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Koin for Android
    implementation("io.insert-koin:koin-android:4.1.1")

    // Koin for Jetpack Compose
    implementation("io.insert-koin:koin-androidx-compose:4.1.1")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Datastore preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // Core Android & Compose Libraries (versions managed by BOM)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)

    // Test Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("io.insert-koin:koin-test:4.1.1")
    testImplementation("io.insert-koin:koin-test-junit4:4.1.1")
    testImplementation("androidx.navigation:navigation-testing:2.9.5")

    // AndroidTest Dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01")) // BOM for tests too
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.5")
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("runUnitTests") {
    group = "verification"
    description = "Runs all unit tests."
    dependsOn("testDebugUnitTest")
}
