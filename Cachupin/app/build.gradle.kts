plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")

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
    implementation("com.google.code.gson:gson:2.8.8")
    // Firebase dependencies
    implementation("com.google.android.gms:play-services-auth:19.2.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation("com.google.firebase:firebase-database:22.0.1")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-messaging:25.0.1")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Room
    implementation(libs.androidx.room.common.jvm)

    // Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.4")

    // Dependencies for Compose
    implementation("androidx.compose.material:material-icons-extended:1.2.0")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("androidx.compose.ui:ui:1.9.5")
    implementation("androidx.compose.material:material:1.9.5")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.5")
    implementation("androidx.compose.runtime:runtime-livedata:1.2.0")
    implementation(platform("androidx.compose:compose-bom:2025.11.01"))
    implementation("androidx.compose.foundation:foundation:1.0.0")
    implementation("androidx.compose.runtime:runtime:1.9.5")
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Camera dependencies
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")

    // Material Components for Android
    implementation("com.google.android.material:material:1.13.0")

    // Activity Compose
    implementation("androidx.activity:activity-compose")

    // Lifecycle Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

    // Debug and testing
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
