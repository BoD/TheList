import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  id("application")
  alias(libs.plugins.dockerJavaApplication)
}

kotlin {
  jvmToolchain(11)
}

application {
  mainClass.set("org.jraf.thelist.server.MainKt")
}

dependencies {
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.defaultHeaders)
  implementation(libs.ktor.server.contentNegotiation)
  implementation(libs.ktor.server.statusPages)
  implementation(libs.ktor.server.callLogging)

  implementation(libs.klibnanolog)

  implementation(project.dependencies.platform(libs.supabase.bom))
  implementation(libs.supabase.auth)
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.realtime)
  implementation(libs.ktor.client.okhttp)

  implementation(libs.openai)
}

docker {
  javaApplication {
    // Use OpenJ9 instead of the default one
    baseImage.set("adoptopenjdk/openjdk11-openj9:x86_64-ubuntu-jre-11.0.30_7.1_openj9-0.57.0")
    maintainer.set("BoD <BoD@JRAF.org>")
    ports.set(listOf(8042))
    images.add("bodlulu/${rootProject.name.lowercase()}:latest")
    jvmArgs.set(listOf("-Xms16m", "-Xmx128m"))
  }
  registryCredentials {
    username.set(System.getenv("DOCKER_USERNAME"))
    password.set(System.getenv("DOCKER_PASSWORD"))
  }
}

tasks.withType<DockerBuildImage> {
  platform.set("linux/amd64")
}

tasks.withType<Dockerfile> {
  // Install ImageMagick
  runCommand("apt-get update")
  runCommand("apt-get install -y imagemagick")

  // Move the COPY instructions to the end
  // See https://github.com/bmuschko/gradle-docker-plugin/issues/1093
  instructions.set(
    instructions.get().sortedBy { instruction ->
      if (instruction.keyword == CopyFileInstruction.KEYWORD) 1 else 0
    }
  )
}

// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image
