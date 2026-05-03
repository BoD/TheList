import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(21)
  jvm {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
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

  android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
    androidResources {
      enable = true
    }
  }

  compilerOptions {
    // See https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields
    freeCompilerArgs.add("-Xexplicit-backing-fields")
    freeCompilerArgs.add("-Xskip-prerelease-check")
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.jetbrains.androidx.lifecycle.viewmodelCompose)
        implementation(libs.jetbrains.androidx.lifecycle.runtimeCompose)

        implementation(project.dependencies.platform(libs.supabase.bom))
        implementation(libs.supabase.auth)
        implementation(libs.supabase.postgrest)
        implementation(libs.supabase.realtime)

        implementation(libs.klibnanolog)
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

    jsMain {
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
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.uiTooling)
}
