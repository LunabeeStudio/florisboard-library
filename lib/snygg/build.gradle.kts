import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(libs.plugins.agp.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    `lunabee-publish`
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

val projectMinSdk: String by project
val projectCompileSdk: String by project


version = Versions.fullVersion
description = "FlorisBoard snygg library"
group = "studio.lunabee.florisboard"

android {
    namespace = "org.florisboard.lib.snygg"
    compileSdk = projectCompileSdk.toInt()

    defaultConfig {
        minSdk = projectMinSdk.toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            create("beta") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(project(":lib:android"))
    implementation(project(":lib:kotlin"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.kotlinx.serialization.json)
}
