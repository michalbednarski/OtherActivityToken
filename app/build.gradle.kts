plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.otheractivitytoken"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.otheractivitytoken"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(project(":provided"))
}