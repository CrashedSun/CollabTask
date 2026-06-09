plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "com.topespinf.collabtask"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.topespinf.collabtask"
        minSdk = 36
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

    dependencies {
    implementation(libs.coreKtx)
    implementation(libs.lifecycleRuntimeKtx)
    implementation(libs.lifecycleViewModelKtx)
    implementation(libs.activityCompose)
    implementation(platform(libs.composeBom))
    implementation(libs.composeUi)
    implementation(libs.composeUiGraphics)
    implementation(libs.composeUiToolingPreview)
    implementation(libs.composeMaterial3)
    implementation(libs.composeMaterialIconsExtended)
    implementation(libs.navigationCompose)
    implementation(libs.material)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAuthKtx)
    implementation(libs.firebaseFirestoreKtx)
    implementation(libs.playServicesAuth)
    implementation(libs.coroutinesAndroid)
    implementation(libs.coroutinesPlayServices)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxJunit)
    androidTestImplementation(libs.espressoCore)

    debugImplementation(libs.composeUiTooling)
    debugImplementation(libs.composeUiTestManifest)
}
