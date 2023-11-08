plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.google.android.gms.samples.wallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.google.android.gms.samples.wallet"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    val lifecycleVersion = "2.6.2"

    implementation("com.google.android.gms:play-services-wallet:19.2.1")
    implementation("com.google.android.gms:play-services-pay:16.3.0")
    implementation("com.google.pay.button:compose-pay-button:0.1.1")
    implementation("com.google.wallet.button:compose-wallet-button:0.1.0-beta01")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-ktx:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
}
