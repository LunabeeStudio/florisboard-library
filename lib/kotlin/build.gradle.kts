import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Copyright 2024 Patrick Goldinger
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

plugins {
    id("java-library")
    id("kotlin")
    `lunabee-publish`
}

version = Versions.fullVersion
description = "FlorisBoard Kotlin utilities"
group = "studio.lunabee.florisboard"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    maybeCreate("main").apply {
        java.srcDir("src/main/kotlin")
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)
}

