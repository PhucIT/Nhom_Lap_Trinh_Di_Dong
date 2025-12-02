
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

//    id("kotlin-kapt")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.expensemanagement"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.expensemanagement"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "17"
//        jvmTarget = "11"


//        freeCompilerArgs = freeCompilerArgs + listOf(
//            "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
//            "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
//        )
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling) 
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.play.services.auth)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Thư viện Firebase và Hilt
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.hilt.android)
//    kapt(libs.hilt.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.compose.material.icons.extended)


    // Vico for charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")

    // Thư viện tạo mã QR
//    implementation("com.github.alexzh.qrcodegen:qrcodegen:3.1.0")
    implementation("io.github.g0dkar:qrcode-kotlin:3.3.0")
    implementation("io.github.g0dkar:qrcode-kotlin-android:3.3.0")
//    implementation("io.github.g0dkar:qrcode-kotlin-compose:4.1.0")
    implementation("com.google.zxing:core:3.5.3")

    // DataStore Preferences để lưu cài đặt chủ đề
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Thư viện cho xác thực sinh trắc học (Vân tay / Khuôn mặt)
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    implementation("androidx.core:core-splashscreen:1.0.1")
}


// THAY THẾ KHỐI KAPT CŨ BẰNG KHỐI NÀY
//kapt {
//    correctErrorTypes = true
//}