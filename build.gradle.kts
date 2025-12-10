// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Project-level build.gradle.kts
plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // Hilt (Dependency Injection)
    id("com.google.dagger.hilt.android") version "2.50" apply false

    // KSP (Required for Room Database) - Matches Kotlin 1.9.0
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}