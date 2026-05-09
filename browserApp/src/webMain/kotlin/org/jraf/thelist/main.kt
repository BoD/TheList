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

package org.jraf.thelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import org.jraf.thelist.ui.main.MainScreen
import org.jraf.thelist.ui.platform.NoOpPlatform
import org.jraf.thelist.ui.theme.AppTheme
import thelist.browserapp.generated.resources.NotoColorEmoji
import thelist.browserapp.generated.resources.Res


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
  ComposeViewport {
    val emojiFont by preloadFont(Res.font.NotoColorEmoji)
    val fontFamilyResolver = LocalFontFamilyResolver.current

    var fontsFallbackInitialized by remember { mutableStateOf(false) }
    if (fontsFallbackInitialized) {
      MainScreen(NoOpPlatform)
    } else {
      // Show an empty screen while the font is loading
      AppTheme {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        )
      }
    }
    LaunchedEffect(emojiFont) {
      if (emojiFont != null) {
        fontFamilyResolver.preload(FontFamily(emojiFont!!))
        fontsFallbackInitialized = true
      }
    }
  }
}
