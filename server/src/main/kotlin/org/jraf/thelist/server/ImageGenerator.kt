/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:OptIn(SupabaseExperimental::class)

package org.jraf.thelist.server

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatModel
import com.openai.models.images.ImageGenerateParams
import com.openai.models.images.ImageModel
import com.openai.models.responses.ResponseCreateParams
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.selectAsFlow
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jraf.klibnanolog.logd
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.io.encoding.Base64

class ImageGenerator(
  private val supabaseSecretKey: String,
  private val openAiApiKey: String,
  private val imagesDir: File,
  private val publicBaseUrl: String,
  private val debug: Boolean = false,
) {
  val supabaseClient: SupabaseClient by lazy {
    createSupabaseClient(
      supabaseUrl = "https://yreptbxulcjtzkymakaz.supabase.co",
      supabaseKey = supabaseSecretKey,
    ) {
      defaultLogLevel = LogLevel.DEBUG

      install(Auth) {
        minimalConfig()
      }
      install(Postgrest)
      install(Realtime)

      if (debug) {
        val config = OkHttpConfig()
        config.config {
          sslSocketFactory(trustAllCertsSslSocketFactory(), trustAllCerts()[0])
          proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(InetAddress.getByName("localhost"), 8888)))
        }
        httpEngine = OkHttpEngine(config)
      }
    }
  }

  @Serializable
  private data class GroceryItem(
    val id: String,
    val name: String,
    val image_url: String?,
  )

  suspend fun run() {
    var cachedGroceryItems: Set<GroceryItem> = emptySet()
    supabaseClient
      .from("grocery_item")
      .selectAsFlow(GroceryItem::id)
      .collect { groceryItems ->
        if (cachedGroceryItems.isEmpty()) {
          cachedGroceryItems = groceryItems.toSet()
        } else {
          val newGroceryItems = groceryItems - cachedGroceryItems
          cachedGroceryItems = groceryItems.toSet()
          val groceryItemsWithoutImageUrls = newGroceryItems.filter { it.image_url == null }
          logd("groceryItemsWithoutImageUrls=$groceryItemsWithoutImageUrls")
          for (groceryItem in groceryItemsWithoutImageUrls) {
            generateAndSaveImage(groceryItem)
          }
        }
      }
  }

  private suspend fun generateAndSaveImage(groceryItem: GroceryItem) {
    withContext(Dispatchers.IO) {
      val temporaryImageFile = File.createTempFile("tmp-${groceryItem.id}", ".png", imagesDir)
      generateImage(groceryItem, temporaryImageFile)
      val finalImageFile = File(imagesDir, "${groceryItem.id}.png")
      resizeImage(temporaryImageFile, finalImageFile)
      temporaryImageFile.delete()
    }
    saveImage(groceryItem)
  }

  private fun resizeImage(temporaryImageFile: File, finalImageFile: File) {
    logd("Resizing image ${temporaryImageFile.absolutePath} to ${finalImageFile.absolutePath}")
    val process = ProcessBuilder(
      "/bin/sh",
      "-c",
      """convert "${temporaryImageFile.absolutePath}" -alpha set -channel A -threshold 10% +channel -trim +repage -resize 64x64 -background none -normalize -ordered-dither o8x8 -gravity center -extent 64x64 "${finalImageFile.absolutePath}"""",
    )
      .start()
    process.waitFor()
    logd("convert process exited with code ${process.exitValue()}")
  }

  private suspend fun saveImage(groceryItem: GroceryItem): PostgrestResult {
    logd("Saving image for grocery item ${groceryItem.name} with id ${groceryItem.id}")
    val imageUrl = "$publicBaseUrl/images/${groceryItem.id}.png"
    return supabaseClient
      .from("grocery_item")
      .update(
        {
          set("image_url", imageUrl)
        },
      ) {
        filter {
          eq("id", groceryItem.id)
        }
      }
  }

  private fun generateImage(
    groceryItem: GroceryItem,
    imageFile: File,
  ) {
    logd("Generating image prompt")
    val client: OpenAIClient = OpenAIOkHttpClient.builder()
      .apiKey(openAiApiKey)
      .build()

    // Create a prompt
    val createImagePromptResponseCreateParams = ResponseCreateParams.builder()
      .model(ChatModel.GPT_5_4_MINI)
      .instructions("")
      .input(
        """
          |Create a prompt for an image generation model to produce an icon representing the item "${groceryItem.name.capitalizeWords()}"
          |for use in a grocery shopping list app.
          |I will prefix the prompt with instructions about the style to make sure all generated images are simple and clear and follow the
          |same style, so I want you to only return the last part of the prompt, without this prefix, which instructs how to represent the
          |item.
          |Do not include any instructions about the style, lighting, background, or anything else, just the part describing how to represent the item.
          |Do not include anything else in your response.
          |""".trimMargin(),
      )
      .build()
    val createImagePromptResponse = client.responses().create(createImagePromptResponseCreateParams)
    val imagePrompt = createImagePromptResponse.output()
      .flatMap { it.message().get().content() }
      .map { it.outputText().get().text() }
      .first()
    logd("imagePrompt=`$imagePrompt`")

    val fullImagePrompt = """
      |Grocery shopping app illustration. Single grocery item, centered on transparent background, realistic but simplified, highly recognizable at
      |small sizes, no text, no logos, soft studio lighting, clean silhouette, professional mobile app asset, no background.
      |
      |Item: $imagePrompt
      |""".trimMargin()

    // Create the image from the prompt
    logd("Generating image")
    val imageGenerateParams = ImageGenerateParams.builder()
      .model(ImageModel.GPT_IMAGE_1_5)
      .size(ImageGenerateParams.Size._1024X1024)
      .background(ImageGenerateParams.Background.TRANSPARENT)
      .outputFormat(ImageGenerateParams.OutputFormat.PNG)
      .quality(ImageGenerateParams.Quality.LOW)
      .prompt(fullImagePrompt)
      .build()
    val imageGenerateResponse = client.images().generate(imageGenerateParams)
    val base64Image = imageGenerateResponse.data().get()
      .map { it.b64Json().get() }
      .first()
    imageFile.writeBytes(Base64.decode(base64Image))
  }

}

private fun trustAllCertsSslSocketFactory() = SSLContext.getInstance("SSL").apply {
  init(null, trustAllCerts(), SecureRandom())
}.socketFactory

private fun trustAllCerts() = arrayOf<X509TrustManager>(
  object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf()
    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
  },
)

private fun String.capitalizeWords(): String {
  return buildString {
    var capitalizeNext = true
    for (char in this@capitalizeWords) {
      if (char.isWhitespace()) {
        capitalizeNext = true
        append(char)
      } else {
        if (capitalizeNext) {
          append(char.uppercaseChar())
          capitalizeNext = false
        } else {
          append(char)
        }
      }
    }
  }
}
