// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false // Match the Android Gradle plugin with your Gradle version
    id("com.android.library") version "8.1.0" apply false    // Match library plugin version
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false // Ensure Kotlin version aligns with AGP
    id("com.google.gms.google-services") version "4.4.2" apply false // Google Services plugin for Firebase
}

allprojects {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
}

// Buildscript block for legacy plugin management (not required for newer Gradle versions)
buildscript {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
    dependencies {
        // Google Services plugin for Firebase
        classpath("com.google.gms:google-services:4.4.2")
    }
}

// Subproject configuration
subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            extensions.getByType<com.android.build.gradle.BaseExtension>().apply {
                compileSdkVersion(34) // Updated to match dependency requirements
                defaultConfig {
                    minSdk = 24 // Update to match project-level `minSdk`
                    targetSdk = 34 // Updated to match dependency requirements
                }
            }
        }
    }
}
