// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
//        maven { url "https://raw.github.com/bmob/bmob-android-sdk/master" }
        maven("https://maven.aliyun.com/repository/google")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("com.tencent.bugly:symtabfileuploader:2.2.1")
        //noinspection DifferentKotlinGradleVersion
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlinVersion}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
allprojects {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
//        maven { url "https://raw.github.com/bmob/bmob-android-sdk/master" }
        maven("https://maven.aliyun.com/repository/google")
    }
}

dependencies {

}
