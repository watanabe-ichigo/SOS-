
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
    // 💥 Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // 🔽 Firebase 必須ライブラリ
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // ✨ アイコンを拡大縮小・切り抜きするライブラリ（これを追加したわよ！）
    implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")

    // 🔥 Glide（画像表示）
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 🔽 Google Services
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    // 🔽 ネットワーク・JSON
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20210307")

    // 🔽 標準ライブラリ
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 🔽 テスト
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}