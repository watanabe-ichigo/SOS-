plugins {
   //alias(libs.plugins.android.application)

    //データベース接続
    id("com.google.gms.google-services")
    id("com.android.application")
}

android {
    namespace = "com.example.sosbaton"
    /*compileSdk {
        version = release(34)
    }*/
    //データベース接続
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sosbaton"
        minSdk = 26
        targetSdk = 36

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
}

dependencies {
    //データベース接続
    //  Firebase Firestore
   /* implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-firestore")*/
    //データベース接続
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Firebase BoM（Firebaseのバージョン管理）
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

// Firestore（これが必要！）
    implementation("com.google.firebase:firebase-firestore")

// 任意：Analytics（使う場合だけ残してOK）
    implementation("com.google.firebase:firebase-analytics")



    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}