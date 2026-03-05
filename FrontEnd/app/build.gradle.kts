plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)                           // ← KSP en vez de kapt (Kotlin 2.0+)
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "com.fitapp.appfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fitapp.appfit"
        minSdk = 31
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Material Design
    implementation(libs.material)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ViewModel + Fragment KTX
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ── OFFLINE: Room ─────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)                         // genera el código de Room

    // ── OFFLINE: WorkManager ──────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ── OFFLINE: Gson (serializar cola de sync) ───────────────
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}