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

package org.jraf.thelist.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jraf.klibnanolog.loge
import org.jraf.thelist.di.AppGraph
import org.jraf.thelist.ui.grocerylist.detail.GroceryListDetailScreen
import org.jraf.thelist.ui.main.MainViewModel.UiState
import org.jraf.thelist.ui.signin.SignInScreen
import org.jraf.thelist.ui.theme.AppTheme

@Composable
fun MainScreen(appGraph: AppGraph) {
  CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
    val viewModel: MainViewModel = metroViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreen(uiState)
  }
}

@Composable
private fun MainScreen(uiState: UiState) {
  val route = when (uiState) {
    UiState.Initializing -> Route.Loading
    UiState.NotAuthenticated -> Route.SignIn
    UiState.Authenticated -> Route.GroceryList
    UiState.RefreshFailure -> Route.RefreshFailure
  }
  LaunchedEffect(uiState) {
    if (uiState == UiState.RefreshFailure) {
      loge("Refresh failure")
    }
  }
  AppTheme {
    NavDisplay(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
      backStack = listOf(route),
      onBack = {},
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = entryProvider {
        entry<Route.Loading> {
          Box(Modifier.fillMaxSize())
        }
        entry<Route.SignIn> {
          SignInScreen()
        }
        entry<Route.GroceryList> {
          GroceryListDetailScreen()
        }
        entry<Route.RefreshFailure> {
          Box(Modifier.fillMaxSize())
        }
      },
    )
  }
}

@Preview
@Composable
private fun MainScreenNotAuthenticatedPreview() {
  MainScreen(uiState = UiState.NotAuthenticated)
}
