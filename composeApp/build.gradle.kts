import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}


room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.androidx.room.runtime)
                implementation(libs.sqlite.bundled)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Supabase Dependencies
                implementation(project.dependencies.platform(libs.supabase.bom))
                implementation(libs.supabase.postgrest)
                implementation(libs.supabase.realtime)
                implementation(libs.supabase.auth)
                implementation(libs.supabase.storage)
                implementation(libs.supabase.kt)

                implementation(libs.ktor.client.websockets)
                implementation(libs.kamel)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.generativeai)

                implementation(libs.kotlinx.datetime)

                implementation(libs.koalaplot.core)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.exifinterface)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.websockets)
                implementation(libs.koin.android)
                implementation(libs.rive.android)
                implementation(libs.android.database.sqlcipher)
                implementation(libs.sqlcipher.android)
                implementation(libs.sqlite.ktx)
                implementation(libs.androidx.core.splashscreen)
                implementation(libs.lottie.compose)
                implementation(libs.kotlinx.datetime)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.ktor.client.websockets)
                implementation(libs.kotlinx.datetime)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.serialization.kotlinx)
                runtimeOnly(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // Conecta iosArm64/iosSimulatorArm64 no iosMain
        iosTargets.forEach { target ->
            val targetMain = target.compilations.getByName("main").defaultSourceSet
            targetMain.dependsOn(iosMain)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.pxrioverde.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Pkg)
            packageName = "Pxrioverde"
            packageVersion = "1.0.0"
        }
    }
}

android {
    namespace = "com.example.pxrioverde"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = true
    }

    packaging {
        jniLibs {
            // Garante que as bibliotecas sejam armazenadas sem compressão e alinhadas
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        applicationId = "com.example.pxrioverde"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        debugImplementation(libs.compose.uiTooling)
        "ksp"(libs.androidx.room.compiler)
        "ksp"(libs.kotlinx.datetime)

        configurations.all {
            resolutionStrategy.dependencySubstitution {
                // Toda vez que um módulo/lib pedir a versão antiga de 4 KB...
                substitute(module("net.zetetic:android-database-sqlcipher"))
                    // ...o Gradle vai substituir pelo pacote novo alinhado em 16 KB
                    .using(module("net.zetetic:sqlcipher-android:4.6.1"))
                    .because("Força o alinhamento de página de 16 KB exigido pelo Android 15+")
            }
        }

    }
}
