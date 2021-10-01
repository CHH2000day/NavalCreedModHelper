plugins {
    id("com.android.library")
    id("kotlin-android")
}
android {
    compileSdk = 30
    defaultConfig {
        minSdk = 23
        targetSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        getByName("release") {
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    compileOnly("androidx.core:core-ktx:1.6.0")
    compileOnly("androidx.appcompat:appcompat:1.3.1")
    compileOnly("com.google.android.material:material:1.4.0")
    compileOnly("com.squareup.okio:okio:3.0.0-alpha.10")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutineVersion}")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    compileOnly("androidx.compose.ui:ui:${Versions.composeVersion}")
    compileOnly("androidx.compose.material:material:${Versions.composeVersion}")
    compileOnly("androidx.compose.ui:ui-tooling-preview:${Versions.composeVersion}")
    compileOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    compileOnly("androidx.activity:activity-compose:1.3.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}