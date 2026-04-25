import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    // alias(libs.plugins.kotlin.compose.compiler)  ←  bez Compose
    alias(libs.plugins.hilt.android)
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val properties = Properties()
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) {
        properties.load(localProperties.inputStream())
    }
    return properties.getProperty(key, defaultValue)
}

android {
    namespace = "pl.edu.anstar.flavorforge"
    compileSdk = 35

    defaultConfig {
        applicationId = "pl.edu.anstar.flavorforge"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${getLocalProperty("supabase.url")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${getLocalProperty("supabase.key")}\"")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            // freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")  ← ZAKOMENTOWANE
        }
    }

    buildFeatures {
        // compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)  // ← klasyczna Activity, nie compose
    implementation(libs.androidx.constraintlayout)

    // COMPOSE
    // implementation(libs.androidx.compose.ui)
    // implementation(libs.androidx.compose.ui.tooling.preview)
    // debugImplementation(libs.androidx.compose.ui.tooling)
    // implementation(libs.androidx.activity.compose)
    // implementation(libs.androidx.lifecycle.runtime.compose)

    // UI & Navigation
    implementation(libs.material)
    // implementation(libs.androidx.material3)  ← Compose Material3 - zakomentowane
    // implementation(libs.androidx.navigation.compose)  ← Navigation Compose - zakomentowane


    // RECYCLERVIEW

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Dependency Injection & Coroutines
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    // implementation(libs.hilt.navigation.compose)  ← tylko dla Compose - zakomentowane
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle ViewModel + LiveData (dla klasycznych View)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}