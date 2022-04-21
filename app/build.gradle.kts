plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("bugly")
    kotlin("plugin.serialization")
}
android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "com.chh2000day.nacvalcreed.modhelper.v4"
        minSdk = 24
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
    flavorDimensions += listOf("transcodeEngine")
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
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutineVersion}")

        implementation("com.google.android.material:material:1.5.0")
        implementation("androidx.appcompat:appcompat:1.4.1")
        implementation("androidx.recyclerview:recyclerview:1.2.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.core:core-ktx:1.7.0")

        implementation("androidx.compose.ui:ui:${Versions.composeVersion}")
        implementation("androidx.compose.material:material:${Versions.composeVersion}")
        implementation("androidx.compose.ui:ui-tooling-preview:${Versions.composeVersion}")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
        implementation("androidx.activity:activity-compose:1.4.0")
        implementation("androidx.navigation:navigation-compose:2.5.0-beta01")


        implementation("com.squareup.okio:okio:${Versions.okioVersion}")
        implementation("com.squareup.okhttp3:okhttp:4.9.3")
        implementation("com.squareup.okhttp3:okhttp-tls:4.9.3")
        implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
        implementation("io.ktor:ktor-client-okhttp:${Versions.ktorVersion}")


        implementation("com.tencent.bugly:crashreport:3.4.4")


        androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.composeVersion}")
        debugImplementation("androidx.compose.ui:ui-tooling:${Versions.composeVersion}")

        "ffmpegImplementation"("com.arthenica:ffmpeg-kit-video:4.5.1")
    }
}

