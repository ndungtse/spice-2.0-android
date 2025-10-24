plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.medtroniclabs.spice.app.analytics"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Make exported Room schemas available to androidTest
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }

        // Match the app’s extra build types so variants wire correctly
        create("staging") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            consumerProguardFiles("proguard-rules.pro")
        }

        create("training") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            consumerProguardFiles("proguard-rules.pro")
        }
    }


    lint {
        disable += setOf("NullSafeMutableLiveData")
        // abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

/** Export Room schemas to analytics/schemas via KAPT */
kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        // arg("room.incremental", "true")
        // arg("room.expandProjection", "true")
    }
}

dependencies {
    // Provide Lifecycle lint checks here too (module-level)
    //lintChecks("androidx.lifecycle:lifecycle-runtime-lint:2.8.6")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    // (Do not use the legacy compiler in Kotlin modules)
    // kapt("android.arch.persistence.room:compiler:1.1.1")

    implementation("com.google.code.gson:gson:2.10.1")

    // Align with app
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
