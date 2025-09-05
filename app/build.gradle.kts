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
    }
}

dependencies {

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Koin for Android
    implementation("io.insert-koin:koin-android:3.4.0")

    // Koin for Jetpack Compose (if you're using Compose)
    implementation("io.insert-koin:koin-androidx-compose:3.4.5")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Datastore preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Datastore preferences
    implementation(libs.androidx.datastore.preferences)

    //implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    // Lifecycles only (without ViewModel or LiveData)
    //implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    // Lifecycle utilities for Compose
    //implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    //libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.datastore.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}