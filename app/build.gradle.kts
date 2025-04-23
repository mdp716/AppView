plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mikasys.appview"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mikasys.appview"
        minSdk = 27
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
    
    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Window insets dependency
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.window:window:1.2.0")
    
    // Add ViewModel dependencies
    implementation("androidx.activity:activity-ktx:1.8.2")  // For viewModels() delegate
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")  // For ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")   // For LiveData
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
}