plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.musicboxd"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.musicboxd"
        minSdk = 24
        targetSdk = 35
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

    dependencies {
        // Core e Compose
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)

        // Navigazione
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // UI classica
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation("androidx.recyclerview:recyclerview:1.3.2")

        // Cronet (networking avanzato)
        implementation("org.chromium.net:cronet-embedded:101.4951.41")

        // HTTP (Retrofit e OkHttp)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:okhttp:4.9.0")

        // Glide (immagini)
        implementation("com.github.bumptech.glide:glide:4.15.1")
        testImplementation(libs.junit.jupiter)
        kapt("com.github.bumptech.glide:compiler:4.15.1")

        // Picasso (se usi anche questo, ma attento: è ridondante con Glide)
        implementation("com.squareup.picasso:picasso:2.71828")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-firestore-ktx:24.7.1")
        implementation("com.google.firebase:firebase-auth:22.1.1")

        // Room (database locale)
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        kapt("androidx.room:room-compiler:2.6.1")

        // Dagger (se lo usi)
        kapt("com.google.dagger:dagger-compiler:2.46")

        // Lifecycle (ViewModel, LiveData)
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

        // Test
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        //Material
        implementation ("com.google.android.material:material:1.12.0")

        implementation("androidx.media3:media3-exoplayer:1.3.1")

        // Per JUnit/Espresso nei test strumentati
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        // Per Mockito nei test strumentati

        androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
        androidTestImplementation ("com.google.firebase:firebase-auth-ktx:22.1.1")

        androidTestImplementation("androidx.test:core:1.5.0")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test:runner:1.5.2")
        androidTestImplementation("androidx.appcompat:appcompat:1.7.0")
        androidTestImplementation("org.mockito:mockito-core:5.11.0")
        androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
        androidTestImplementation("org.mockito:mockito-inline:5.2.0")

        debugImplementation("androidx.fragment:fragment-testing:1.8.2")

        androidTestImplementation("androidx.test:core:1.5.0")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test:runner:1.5.2")
        androidTestImplementation("com.google.android.material:material:1.12.0")


    }

