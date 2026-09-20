plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val releaseVersionName = providers.environmentVariable("VERSION_NAME").orNull
    ?.takeIf { it.isNotBlank() } ?: "1.0.0"
val releaseVersionCode = providers.environmentVariable("VERSION_CODE").orNull
    ?.toIntOrNull()?.takeIf { it > 0 } ?: 1
val apiBaseUrl = providers.environmentVariable("API_BASE_URL").orNull
    ?.takeIf { it.isNotBlank() } ?: "https://cupear.i96.me/api/me"
val releaseKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull

android {
    namespace = "com.zxm965.cullpear"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zxm965.cullpear"
        minSdk = 37
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName

    }

    signingConfigs {
        if (!releaseKeystorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.replace("\"", "\\\"")}\"")
    }
    sourceSets {
        getByName("test") {
            kotlin.directories.add("../tests/unit")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
