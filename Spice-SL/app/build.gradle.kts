plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.medtroniclabs.spice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.medtroniclabs.spice"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "spice"
            keyPassword = "spice@123"
            storePassword = "spice@123"
            storeFile = file("spice")
        }
        create("staging") {
            keyAlias = "spice"
            keyPassword = "spice@123"
            storePassword = "spice@123"
            storeFile = file("spice")
        }
    }

    buildTypes {

        release {
            applicationIdSuffix = ".sl.staging"
            versionNameSuffix = "-(20240813_01)"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            applicationIdSuffix = ".sl.staging"
            isDebuggable = true
            // below line for test the forgot password in mobile
         //  signingConfig = signingConfigs.getByName("release")
        }

        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".sl.staging"
            versionNameSuffix = "-(20240823_01)"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("staging")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.hilt:hilt-common:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("androidx.activity:activity:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    //Room database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    //Kotlin coroutine dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.0")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Flexbox Layout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    //Lottie
    implementation("com.airbnb.android:lottie:6.2.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("joda-time:joda-time:2.12.5")

    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    //Swipe Refresh Layout
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Security hashing
    implementation ("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    //Workmanager
    implementation ("androidx.work:work-runtime:2.9.0")

    //Local date
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // Pagination
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
}