plugins {
    // Android Gradle plugin (kept up-to-date with the stable release)
    id("com.android.application") version "8.1.0" apply true
    // Kotlin plugin for Android
    id("org.jetbrains.kotlin.android") version "1.9.10" apply true
    // Google services plugin for Firebase integration
    id("com.google.gms.google-services")
}

android {
    // The namespace for the app’s package
    namespace = "com.example.semester_project_cis_357_task_notification"
    compileSdk = 34 // Target the latest SDK for new features and better compatibility

    defaultConfig {
        applicationId = "com.example.semester_project_cis_357_task_notification"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Default test runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use vector drawables support library
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Not minifying for now; can enable ProGuard/R8 later if needed
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Ensure that we use Java 8 compatibility
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        // Target Java 8 bytecode
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true // Enable Jetpack Compose
        buildConfig = true // Explicitly enable BuildConfig
    }

    composeOptions {
        // Align with a stable, known-good Compose compiler extension version
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packagingOptions {
        // Exclude certain license files to avoid packaging conflicts
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Use the Compose BOM to ensure versions of Compose libraries are aligned
    implementation(platform("androidx.compose:compose-bom:2023.09.01"))

    // Compose UI and related libraries without explicit versions, relying on the BOM
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // No explicit version, BOM manages it
    implementation("androidx.compose.material:material-icons-extended")

    // Tooling for UI previews/debugging
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Firebase dependencies with BOM for version alignment
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Navigation dependencies
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.2")
    implementation("androidx.navigation:navigation-compose:2.7.2")

    // Classic Android UI libraries, compatible with newer versions
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Use the same Compose BOM for Android tests to keep versions aligned
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// Apply the Google services plugin for Firebase
apply(plugin = "com.google.gms.google-services")
