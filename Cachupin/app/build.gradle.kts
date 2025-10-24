plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.cachupin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cachupin"
        minSdk = 24
        targetSdk = 36
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
    // Compose dependencies
    implementation("androidx.compose.ui:ui:1.0.0")
    implementation("androidx.compose.material:material:1.0.0")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.foundation:foundation:1.0.0") // If needed
    implementation("androidx.compose.runtime:runtime:1.0.0") // If needed

    // Camera dependencies
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")

    // Material Components for Android
    implementation("com.google.android.material:material:1.6.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Activity Compose
    implementation("androidx.activity:activity-compose")

    // Lifecycle Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

    // Debug and testing
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // JUnit for testing
    testImplementation("junit:junit:4.13.2")  // Cambia según la versión de JUnit que uses
    androidTestImplementation("androidx.junit:jest:1.1.2") // Cambia a la versión que sea compatible
    androidTestImplementation("androidx.espresso:espresso-core:3.4.0")

    // Debug tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
