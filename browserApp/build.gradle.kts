import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xskip-prerelease-check")
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":shared"))

      implementation(libs.compose.ui)
      implementation(libs.compose.foundation)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.material3)
    }
  }
}

// `./gradlew wasmJsBrowserDevelopmentRun --continuous` to run the dev server in continuous mode (should open `http://localhost:8080/`)
// `./gradlew wasmJsBrowserDevelopmentExecutableDistribution` to build the dev distribution, results are in `build/dist/wasmJs/developmentExecutable`
// `./gradlew wasmJsBrowserDistribution` to build the release distribution, results are in `build/dist/wasmJs/productionExecutable`
