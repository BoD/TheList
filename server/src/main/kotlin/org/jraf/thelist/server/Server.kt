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

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.routing
import org.slf4j.event.Level
import java.io.File


class Server(
  private val port: Int,
  private val imagesDir: File,
) {
  suspend fun run() {
    embeddedServer(
      factory = Netty,
      configure = {
        connectors.add(
          EngineConnectorBuilder().apply {
            port = this@Server.port
          },
        )
      },
      module = { mainModule() },
    ).startSuspend(wait = true)
  }

  private fun Application.mainModule() {
    install(DefaultHeaders)

    install(CallLogging) {
      level = Level.DEBUG
    }

    install(StatusPages)

    install(ContentNegotiation)

    routing {
      staticFiles("/images", imagesDir)
    }
  }
}
