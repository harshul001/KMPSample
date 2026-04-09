import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "NetworkSDK"
            isStatic = true
        }
    }

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.okhttp)
            implementation(libs.okhttp.logging)
        }

        jsMain.dependencies {
            // Kotlin/JS fetch API is built-in, no additional dependencies needed
        }

        wasmJsMain.dependencies {
            // Kotlin/Wasm fetch API is built-in
        }

        iosMain.dependencies {
            // iOS uses Kotlin's built-in networking APIs
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.example"
            artifactId = "network-sdk"
            version = "1.0.0"

            // Kotlin Multiplatform automatically creates components via publishLibraryVariants
        }
    }

    repositories {
        maven {
            name = "LocalMaven"
            url = uri("${layout.buildDirectory}/repo")
        }
        
        // JFrog Artifactory configuration (uncomment and configure for production)
        // maven {
        //     name = "Artifactory"
        //     url = uri("https://your-artifactory.jfrog.io/artifactory/libs-release/")
        //     credentials {
        //         username = project.findProperty("artifactoryUsername") as String? ?: ""
        //         password = project.findProperty("artifactoryPassword") as String? ?: ""
        //     }
        // }
    }
}

