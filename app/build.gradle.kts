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
//        viewBinding = true
    }
    dependencies {
//        val ffmpeg by configurations
        implementation("com.google.android.material:material:1.4.0")
        implementation("androidx.appcompat:appcompat:1.3.1")
        implementation("androidx.recyclerview:recyclerview:1.2.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.squareup.okio:okio:2.10.0")
        implementation("com.squareup.okhttp3:okhttp:4.9.0")
        implementation("com.squareup.okhttp3:okhttp-tls:4.9.0")
        implementation("com.tencent.bugly:crashreport:3.4.4")
        implementation("androidx.core:core-ktx:1.6.0")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerializationVersion}") // JVM dependency
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerializationVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutineVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

        "ffmpegImplementation"("com.arthenica:mobile-ffmpeg-video:4.4.LTS")
    }
}

