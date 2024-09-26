plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id ("androidx.navigation.safeargs.kotlin")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")


}

android {
    namespace = "com.example.airytics"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.airytics"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures{
        viewBinding = true
        dataBinding = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Lottie
    implementation ("com.airbnb.android:lottie:3.4.0")

    //navigation

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.0-beta01")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.0-beta01")

    //google services
    implementation ("com.google.android.gms:play-services-location:17.1.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    ///room database
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    implementation ("com.google.code.gson:gson:2.10.1")  // Add this line for Gson


    //OSM
    implementation(libs.osmdroid.android)

    //testing
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("app.cash.turbine:turbine:1.0.0")
    testImplementation ("org.robolectric:robolectric:4.8")
    testImplementation ("org.mockito:mockito-core:3.12.4")

    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")

    // For coroutines testing
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")




}