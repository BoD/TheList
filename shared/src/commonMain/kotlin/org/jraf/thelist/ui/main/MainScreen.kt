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

@file:OptIn(ExperimentalMaterial3Api::class)

package org.jraf.thelist.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jraf.klibnanolog.logd
import org.jraf.klibnanolog.loge
import org.jraf.thelist.ui.grocerylist.detail.GroceryListDetailScreen
import org.jraf.thelist.ui.platform.NoOpPlatform
import org.jraf.thelist.ui.platform.Platform
import org.jraf.thelist.ui.signin.SignInScreen
import org.jraf.thelist.ui.theme.AppTheme

@Composable
fun MainScreen(platform: Platform) {
  val viewModel = viewModel { MainViewModel() }
  val state by viewModel.state.collectAsState()
  MainScreen(platform, state)
}

@Composable
private fun MainScreen(platform: Platform, state: MainViewModel.State) {
  AppTheme {
    when (state) {
      MainViewModel.State.Initializing -> {
        logd("MainScreen: Initializing")
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        )
      }

      MainViewModel.State.NotAuthenticated -> {
        logd("MainScreen: Not authenticated")
        SignInScreen()
      }

      MainViewModel.State.Authenticated -> {
        logd("MainScreen: Authenticated")
        GroceryListDetailScreen(platform)
      }

      MainViewModel.State.RefreshFailure -> {
        loge("Refresh failure")
      }
    }
  }
}

@Preview
@Composable
private fun MainScreenNotAuthenticatedPreview() {
  MainScreen(platform = NoOpPlatform, state = MainViewModel.State.NotAuthenticated)
}
