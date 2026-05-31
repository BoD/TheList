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

package org.jraf.thelist.server

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

private const val DEFAULT_PORT = 8042

private const val ENV_PORT = "PORT"
private const val ENV_PUBLIC_BASE_URL = "PUBLIC_BASE_URL"
private const val ENV_IMAGES_DIR = "IMAGES_DIR"
private const val ENV_SUPABASE_SECRET_KEY = "SUPABASE_SECRET_KEY"
private const val ENV_OPENAI_API_KEY = "OPENAI_API_KEY"

class Main {
  private val publicBaseUrl = System.getenv(ENV_PUBLIC_BASE_URL)?.trimEnd('/')
    ?: error("PUBLIC_BASE_URL environment variable is not set. This is the URL that the server will be accessible at, e.g. https://example.com")

  private val imagesDir: File = File(System.getenv(ENV_IMAGES_DIR) ?: "/images").also { it.mkdirs() }

  private val port = System.getenv(ENV_PORT)?.toInt() ?: DEFAULT_PORT

  private val supabaseSecretKey = System.getenv(ENV_SUPABASE_SECRET_KEY)
    ?: error("SUPABASE_SECRET_KEY environment variable is not set. This is the secret key of your Supabase project, used to access the database and storage.")

  private val openAiApiKey = System.getenv(ENV_OPENAI_API_KEY)
    ?: error("OPENAI_API_KEY environment variable is not set. This is the API key of your OpenAI account, used to access the OpenAI API.")

  suspend fun run() {
    val server = Server(
      port = port,
      imagesDir = imagesDir,
    )

    val imageGenerator = ImageGenerator(
      publicBaseUrl = publicBaseUrl,
      imagesDir = imagesDir,
      supabaseSecretKey = supabaseSecretKey,
      openAiApiKey = openAiApiKey,
    )

    coroutineScope {
      launch {
        server.run()
      }
      imageGenerator.run()
    }
  }
}

suspend fun main() {
  Main().run()
}
