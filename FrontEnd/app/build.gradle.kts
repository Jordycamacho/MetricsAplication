plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
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

    // ── Source Sets ───────────────────────────────────────────────────────────
    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/res-shared",
                "src/main/res-auth",
                "src/main/res-home",
                "src/main/res-routine",
                "src/main/res-exercise",
                "src/main/res-workout",
                "src/main/res-profile",
                "src/main/res-subscription",
                "src/main/res-sport",
                "src/main/res-parameter",
                "src/main/res-training",
                "src/main/res-importexport",
                "src/main/res-metrics",
                "src/main/res-marketplace",
                "src/main/res-category"
            )
        }
    }

    // ── Entornos ──────────────────────────────────────────────────────────────
    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.14:8080/\"")
        }
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.13:8082/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.jnobfit.online/\"")
        }
    }

    // ── Tipos de build ────────────────────────────────────────────────────────
    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    implementation("androidx.browser:browser:1.8.0")

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

    // ── OFFLINE: Room ──────────────────────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ── OFFLINE: WorkManager ───────────────────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ── OFFLINE: Gson ──────────────────────────────────────────────────────────
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}