import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    jvmTarget = JvmTarget.JVM_25
    freeCompilerArgs.add("-Xskip-prerelease-check")
  }
}

dependencies {
  implementation(project(":shared"))

  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutinesSwing)

  implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
  application {
    mainClass = "org.example.project.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "org.example.project"
      packageVersion = "1.0.0"
    }
  }
}
