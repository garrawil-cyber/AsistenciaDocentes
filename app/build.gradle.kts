plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "pe.colegio.asistencia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.colegio.asistencia"
        minSdk = 24
        targetSdk = 37
        versionCode = 2  // 1: desarrollo, 2: primera distribución
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    // ==========================================
    // Compose (usa el BOM para versiones)
    // ==========================================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ==========================================
    // Iconos extendidos (ArrowBack, etc.)
    // Sin versión → la hereda del BOM de Compose
    // ==========================================
    implementation("androidx.compose.material:material-icons-extended")

    // ==========================================
    // Testing
    // ==========================================
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ==========================================
    // Retrofit + OkHttp (HTTP cliente)
    // ==========================================
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ==========================================
    // Coroutines
    // ==========================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ==========================================
    // ViewModel + Lifecycle
    // ==========================================
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    // ==========================================
    // DataStore (persistencia de JWT)
    // ==========================================
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ==========================================
    // Navigation Compose
    // ==========================================
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ==========================================
    // CameraX (cámara en vivo)
    // ==========================================
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ==========================================
    // ML Kit Barcode Scanning (lector QR)
    // ==========================================
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ==========================================
    // Accompanist Permissions (permisos en Compose)
    // ==========================================
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    }

