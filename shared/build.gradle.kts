import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
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

        implementation(project.dependencies.platform("io.github.jan-tennert.supabase:bom:3.5.0"))
        implementation("io.github.jan-tennert.supabase:auth-kt")
        implementation("io.github.jan-tennert.supabase:postgrest-kt")
        implementation("io.github.jan-tennert.supabase:realtime-kt")

        implementation("org.jraf.klibnanolog:klibnanolog:1.2.0")
      }
    }

    jvmMain {
      dependencies {
        implementation("io.ktor:ktor-client-okhttp:3.4.2")
      }
    }

    androidMain {
      dependencies {
        implementation(libs.compose.uiToolingPreview)

        implementation("io.ktor:ktor-client-okhttp:3.4.2")
      }
    }

    jsMain {
      dependencies {
        implementation(libs.wrappers.browser)

        implementation("io.ktor:ktor-client-js:3.4.2")
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
