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

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.library()

//    // See https://youtrack.jetbrains.com/issue/CMP-4906
//    binaries.executable()
  }

  android {
    namespace = "org.jraf.thelist.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
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
      dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        api(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.jetbrains.androidx.lifecycle.viewmodelCompose)
        implementation(libs.jetbrains.androidx.lifecycle.runtimeCompose)

        implementation(project.dependencies.platform(libs.supabase.bom))
        implementation(libs.supabase.auth)
        implementation(libs.supabase.postgrest)
        implementation(libs.supabase.realtime)

        implementation(libs.klibnanolog)

        implementation(libs.coil.compose)
        implementation(libs.coil.ktor3)
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
