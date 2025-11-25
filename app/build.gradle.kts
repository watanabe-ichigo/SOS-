
//経路選択
import java.util.Properties
plugins {
    //alias(libs.plugins.android.application)



    //データベース接続
    id("com.google.gms.google-services")
    id("com.android.application")
}

android {

    buildFeatures {
        buildConfig = true
    }



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
        // 🔐 local.properties から API キーを読み込み
        val props = Properties()
        props.load(project.rootDir.resolve("local.properties").inputStream())

        // === Gemini Key 読み込み ===
        val geminiKey = props.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")

        // === Google Maps/Directions Key 読み込み ===
        val mapsKey = props.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")



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
    //implementation("com.google.firebase:firebase-analytics")

    // 🔽 ここにこれを追加！！
    implementation("com.google.firebase:firebase-auth")
    //経路選択
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20210307")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")



    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //位置情報取得ピン立て処理
    implementation("com.google.android.gms:play-services-location:21.0.1")
}