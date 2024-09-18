/*
 * Copyright (C) 2022 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Suppress needed until https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    id(libs.plugins.agp.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.compose.compiler)
}

val projectMinSdk: String by project
val projectTargetSdk: String by project
val projectCompileSdk: String by project
val projectBuildToolsVersion: String by project
val projectNdkVersion: String by project
val projectVersionCode: String by project
val projectVersionName: String by project
val projectVersionNameSuffix = projectVersionName.substringAfter("-", "")

android {
    namespace = "dev.patrickgold.florisboard"
    compileSdk = projectCompileSdk.toInt()
    buildToolsVersion = projectBuildToolsVersion
    ndkVersion = projectNdkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = "dev.patrickgold.florisboard"
        minSdk = projectMinSdk.toInt()
        targetSdk = projectTargetSdk.toInt()
        versionCode = projectVersionCode.toInt()
        versionName = projectVersionName.substringBefore("-")

        buildConfigField("String", "BUILD_COMMIT_HASH", "\"${getGitCommitHash()}\"")

        sourceSets {
            maybeCreate("main").apply {
                java {
                    srcDirs("src/main/kotlin")
                }
            }
        }
    }

    bundle {
        language {
            // We disable language split because FlorisBoard does not use
            // runtime Google Play Service APIs and thus cannot dynamically
            // request to download the language resources for a specific locale.
            enableSplit = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug+${getGitCommitHash(short = true)}"

            isDebuggable = true
            isJniDebuggable = false
        }

        create("beta") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        named("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-Xjvm-default=all-compatibility",
        )
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(project(":florisboard-library"))

    androidTestImplementation(libs.androidx.test.espresso.core)
}

fun getGitCommitHash(short: Boolean = false): String {
    if (!File(".git").exists()) {
        return "null"
    }

    val stdout = ByteArrayOutputStream()
    exec {
        if (short) {
            commandLine("git", "rev-parse", "--short", "HEAD")
        } else {
            commandLine("git", "rev-parse", "HEAD")
        }
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
