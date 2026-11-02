@file:OptIn(ExperimentalTime::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

val generateAppInfoKtTask: TaskProvider<Task> = tasks.register("generateAppInfoKt") {
  description = "Generates a AppInfo.kt file with a few constants"
  val outputDir = layout.buildDirectory.dir("generated/source/kotlin").get().asFile
  outputs.dir(outputDir)
  doFirst {
    val outputWithPackageDir = File(outputDir, "org/jraf/thelist").apply { mkdirs() }
    File(outputWithPackageDir, "AppInfo.kt").writeText(
      """
        package org.jraf.thelist
        const val BUILD_DATE = "${Clock.System.now().toString().replace('T', ' ').take(16)}"
      """.trimIndent()
    )
  }
}

kotlin {
  jvmToolchain(21)
  jvm {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.library()

//    // See https://youtrack.jetbrains.com/issue/CMP-4906
//    binaries.executable()
  }

  android {
    namespace = "org.jraf.thelist.shared"
    compileSdk {
      version = release(libs.versions.android.compileSdk.major.get().toInt()) {
        minorApiLevel = libs.versions.android.compileSdk.minor.get().toInt()
      }
    }
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
    androidResources {
      enable = true
    }
    withHostTest {
      isIncludeAndroidResources = true
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "Shared"
      isStatic = true
    }
  }

  compilerOptions {
    // See https://kotlinlang.org/docs/whatsnew24.html#support-for-collection-literals
    freeCompilerArgs.add("-Xcollection-literals")
  }

  sourceSets {
    commonMain {
      kotlin.srcDir(generateAppInfoKtTask)

      dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.jetbrains.androidx.lifecycle.viewmodelCompose)
        implementation(libs.jetbrains.androidx.lifecycle.runtimeCompose)
        implementation(libs.jetbrains.androidx.lifecycle.viewmodelNavigation3)
        implementation(libs.jetbrains.androidx.navigation3.ui)

        api(project.dependencies.platform(libs.supabase.bom))
        api(libs.supabase.auth)
        implementation(libs.supabase.postgrest)
        implementation(libs.supabase.realtime)

        implementation(libs.klibnanolog)

        implementation(libs.coil.compose)
        implementation(libs.coil.ktor3)

        api(libs.metrox.viewModelCompose)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.ktor.client.okhttp)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.ktor.client.okhttp)
      }
    }

    wasmJsMain {
      dependencies {
        implementation(libs.wrappers.browser)
        implementation(libs.ktor.client.js)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.uiTooling)
}
