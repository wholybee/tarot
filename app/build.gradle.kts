plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.devtools.ksp")
    id ("org.jetbrains.kotlin.plugin.serialization")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "net.holybee.tarot"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.holybee.tarot"
        minSdk = 27
        targetSdk = 34
        versionCode = 8
        versionName = "1.1"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation ("com.aallam.openai:openai-client:3.3.0")
    implementation ("io.ktor:ktor-client-core:2.3.2")
    implementation ("io.ktor:ktor-client-cio:2.3.2")
    implementation ("org.slf4j:slf4j-log4j12:2.0.7")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.preference:preference:1.2.0")

    implementation ("androidx.room:room-runtime:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    ksp  ("androidx.room:room-compiler:2.5.2")
    implementation("com.android.billingclient:billing:6.0.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation ("com.google.android.gms:play-services-auth:20.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
//    implementation ("com.facebook.android:facebook-android-sdk:16.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}