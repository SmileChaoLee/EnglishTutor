plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {

    signingConfigs {
        create("release") {
            storeFile = file("/home/chaolee/AndroidStudioProjects/keystore/new_key.jks")
            storePassword = "86637971"
            keyAlias = "chaolee1959"
            keyPassword = "86637971"
        }
    }

    namespace = "com.smile.englishtutor"

    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.smile.englishtutor"
        minSdk = 24
        targetSdk = 37
        versionCode = 111
        versionName = "1.11"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    configurations.all {
        resolutionStrategy {
            force(libs.androidx.work.runtime.ktx)
            force(libs.androidx.room.runtime)
            force(libs.androidx.sqlite.framework)
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // implementation(libs.play.services.ads.api)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // For coroutines (optional, but recommended for modern Android development)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit.coroutines.adapter)

    implementation (libs.billing.ktx)
    implementation (libs.firebase.core)
    implementation (libs.firebase.ads)
    implementation (libs.user.messaging.platform)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.sqlite.framework)
    // Kotlin DSL (build.gradle.kts)
    implementation(files("libs/smilelibraries-release.aar"))
    implementation(files("libs/NativeTemplatesModels-release.aar"))

    implementation(libs.androidyoutubeplayer.core)
    implementation(libs.androidyoutubeplayer.custom.ui)
    implementation(libs.coil.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}