plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mikasys.appview"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mikasys.appview"
        minSdk = 27
        targetSdk = 35
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
}

// build.gradle.kts (Kotlin DSL)
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview) // Correct alias
    implementation(libs.androidx.lifecycle.runtime.ktx) // Correct alias

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // ksp("com.github.bumptech.glide:ksp:4.16.0") // For Kotlin KSP (preferred over annotationProcessor)

    // Kotlin Coroutines (Optional but recommended for background tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Or latest
}