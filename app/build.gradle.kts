plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.angels.notes"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.angels.notes"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    
    // ──────────────────────────────────────────────────────────
    // TAMBAHAN LIBRARY UNTUK UAS (PASANG SEKARANG)
    // ──────────────────────────────────────────────────────────
    // Retrofit untuk koneksi HTTP API JSON (Tugas Ivan)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // GSON untuk parsing data JSON otomatis (Tugas Ivan)
    implementation("com.google.code.gson:gson:2.8.9")
    
    // Fragment KTX agar Siti gampang transaksi Fragment (Tugas Siti)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // ──────────────────────────────────────────────────────────

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}