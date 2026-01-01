
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
    // 💥 Firebase BoM（バージョン管理の土台。最新版を使うのだ！）
    // 34.5.0が2回あったのを1つにまとめたのだ
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // 🔽 Firebase 必須ライブラリ（BoMでバージョン指定を省略できるのだ）
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // 🔥 Firebase Storage（バージョンが重複していたので、BoMに頼るためにバージョン指定を削除するのだ！）
    // implementation("com.google.firebase:firebase-storage:20.3.0") と
    // implementation 'com.google.firebase:firebase-storage:20.1.0' は両方削除なのだ
    implementation("com.google.firebase:firebase-storage")

    // 🔥 Glide（アイコン画像表示のため）
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 🔽 Google Services（Play Services, Maps, Location）
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:2.3.0") // マップユーティリティ

    // 🔽 その他
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20210307")

    // 🔽 標準的なAndroidライブラリ
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // constraintlayoutが2回あったので1つ削除
    // implementation("androidx.constraintlayout:constraintlayout:2.1.4") // これを削除

    // 任意：Analytics（使わないならコメントアウトでOK）
    implementation("com.google.firebase:firebase-analytics")

    // 🔽 テスト
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}