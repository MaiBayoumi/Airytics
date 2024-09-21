// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version ("2.0.1") apply false
    id ("androidx.navigation.safeargs") version ("2.6.0") apply false
}