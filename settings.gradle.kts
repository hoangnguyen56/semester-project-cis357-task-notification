pluginManagement {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
        gradlePluginPortal() // Gradle Plugin Portal for additional plugins
    }

    plugins {
        id("com.android.application") version "8.1.0" // Align with the stable version of the Android Gradle Plugin
        id("org.jetbrains.kotlin.android") version "1.9.10" // Ensure compatibility with the Kotlin plugin
        id("com.google.gms.google-services") version "4.4.2" // Firebase plugin version
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Prefer repositories defined here
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
}

rootProject.name = "semester-project-cis-357-task-notification"

// Include modules
include(":app")
include(":functions") // Ensure the module name is correct; use "functions" if it's the Firebase Functions folder
