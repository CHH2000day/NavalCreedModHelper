plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("bugly")
    kotlin("plugin.serialization")
}
android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.chh2000day.nacvalcreed.modhelper.v4"
        minSdk = 21
        targetSdk = 30
        versionCode = 110
        versionName = "build-2021092200"
        buildConfigField("int", "BuildVersion", "10106")
        ndk {
            abiFilters += setOf("arm64-v8a", "x86")
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile =
                file(SignInfo.SIGN_FILE)
            storePassword = SignInfo.STORE_PWD
            keyAlias = SignInfo.KEY_ALIAS
            keyPassword = SignInfo.KEY_PWD
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfigs {
                signingConfig = getByName("release")
            }
        }
        getByName("debug") {
            signingConfigs {
                signingConfig = getByName("release")
            }
        }
    }
    flavorDimensions("transcodeEngine")
    productFlavors {
        create("common") {
            dimension = "transcodeEngine"
            versionNameSuffix = "-common"
            matchingFallbacks += listOf("release", "debug")
        }
        create("ffmpeg") {
            dimension = "transcodeEngine"
            versionNameSuffix = "-ffmpeg"
            matchingFallbacks += listOf("release", "debug")
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerializationVersion}") // JVM dependency
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerializationVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutineVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

        implementation("com.google.android.material:material:1.4.0")
        implementation("androidx.appcompat:appcompat:1.3.1")
        implementation("androidx.recyclerview:recyclerview:1.2.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.core:core-ktx:1.6.0")

        implementation("androidx.compose.ui:ui:${Versions.composeVersion}")
        implementation("androidx.compose.material:material:${Versions.composeVersion}")
        implementation("androidx.compose.ui:ui-tooling-preview:${Versions.composeVersion}")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
        implementation("androidx.activity:activity-compose:1.3.1")

        implementation("com.squareup.okio:okio:3.0.0-alpha.10")
        implementation("com.squareup.okhttp3:okhttp:4.9.0")
        implementation("com.squareup.okhttp3:okhttp-tls:4.9.0")

        implementation("com.tencent.bugly:crashreport:3.4.4")


        androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.composeVersion}")
        debugImplementation("androidx.compose.ui:ui-tooling:${Versions.composeVersion}")

        "ffmpegImplementation"("com.arthenica:mobile-ffmpeg-video:4.4.LTS")
    }
}

