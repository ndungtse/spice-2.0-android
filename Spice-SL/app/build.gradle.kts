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
    var commonPrefix = "SPICE"



    defaultConfig {
        applicationId = "com.medtroniclabs.spice"
        minSdk = 23
        targetSdk = 34
        versionCode = 6
        versionName = "2.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "version"
    productFlavors {
        create("sl") {
            dimension = "version"
            applicationIdSuffix = ".sl"
            commonPrefix = "SPICE 2.0.1"
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = "medtronic"
            keyPassword = "Med@Tr0ni#Lab$"
            storePassword = "Med@Tr0ni#Lab$"
            storeFile = file("spice_sl.jks")
        }
        create("africa") {
            keyAlias = "medtronic"
            keyPassword = "Med@Tr0ni#Lab$"
            storePassword = "Med@Tr0ni#Lab$"
            storeFile = file("spice_prod.jks")
        }
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationVariants.all {
                if (buildType.name == "release") {
                    manifestPlaceholders["appNameSuffix"] = commonPrefix
                    if (productFlavors[0].name == "sl") {
                        buildConfigField("String", "API_BASE_URL", "\"https://spice-backend.sl.medtroniclabs.org/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://spiceadmin.sl.medtroniclabs.org/\"")
                        buildConfigField("String", "SALT", "\"Sp!(e_PrOD_II\"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    } else if (productFlavors[0].name == "africa") {
                        manifestPlaceholders["appNameSuffix"] = commonPrefix
                        buildConfigField("String", "API_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "SALT", "\" \"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    }
                }
            }
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isDebuggable = true
            applicationIdSuffix = ".dev"
            applicationVariants.all {
                if (buildType.name == "debug") {
                    manifestPlaceholders["appNameSuffix"] = "$commonPrefix Dev"
                    if (productFlavors[0].name == "sl") {
                        buildConfigField("String", "API_BASE_URL", "\"https://spice-dev-backend.sl.labsplatform.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://spice-dev.sl.labsplatform.com/\"")
                        buildConfigField("String", "SALT", "\"spice_uat\"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    } else if (productFlavors[0].name == "africa") {
                        buildConfigField("String", "API_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "SALT", "\" \"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    }
                }
            }
            signingConfig = signingConfigs.getByName("staging")
        }

        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-(20240830_01)"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationVariants.all {
                if (buildType.name == "staging") {
                    manifestPlaceholders["appNameSuffix"] = "$commonPrefix Staging"
                    if (productFlavors[0].name == "sl") {
                        buildConfigField("String", "API_BASE_URL", "\"https://spice-dev-backend.sl.labsplatform.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://spice-dev.sl.labsplatform.com/\"")
                        buildConfigField("String", "SALT", "\"spice_uat\"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    } else if (productFlavors[0].name == "africa") {
                        buildConfigField("String", "API_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "SALT", "\" \"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    }
                }
            }
            signingConfig = signingConfigs.getByName("staging")
        }

        create("training") {
            initWith(getByName("release"))
            applicationIdSuffix = ".training"
            versionNameSuffix = "-(20240829_01)"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationVariants.all {
                if (buildType.name == "training") {
                    manifestPlaceholders["appNameSuffix"] = "$commonPrefix Training"
                    if (productFlavors[0].name == "sl") {
                        buildConfigField("String", "API_BASE_URL", "\"https://spice-training-backend.sl.labsplatform.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://spice-training.sl.labsplatform.com/\"")
                        buildConfigField("String", "SALT", "\"spice_uat\"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    } else if (productFlavors[0].name == "africa") {
                        buildConfigField("String", "API_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "ADMIN_BASE_URL", "\"https://localhost.com/\"")
                        buildConfigField("String", "SALT", "\" \"")
                        buildConfigField("String", "DB_PASSWORD", "\"Med@Tr0ni#Lab$\"")
                    }
                }
            }
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
        buildConfig = true
    }

    flavorDimensions += "version"
    productFlavors {
        create("sl") {
            dimension = "version"
            applicationIdSuffix = ".sl"
            signingConfig = signingConfigs.getByName("sl")
        }
        create("africa") {
            dimension = "version"
            versionCode = 15
            versionName = "2.1.0"
            signingConfig = signingConfigs.getByName("africa")
        }
    }
}

dependencies {

    implementation(project(":analytics"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.hilt:hilt-common:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
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
    implementation("com.airbnb.android:lottie:6.5.0")

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

    implementation("net.zetetic:sqlcipher-android:4.6.0@aar")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    //in-app update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // NCD
    implementation("com.github.jeffreyliu8:FlexBoxRadioGroup:0.0.8") {
        exclude(group = "com.google.android", module = "flexbox")
    }
}