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

package org.example.project.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.ui.grocerylist.detail.GroceryListDetailScreen
import org.example.project.ui.signin.SignInScreen
import org.example.project.ui.theme.AppTheme

@Composable
fun MainScreen() {
  val viewModel = viewModel { MainViewModel() }
  val state by viewModel.state.collectAsState()
  MainScreen(state)
}

@Composable
private fun MainScreen(state: MainViewModel.State) {
  AppTheme {
    when (state) {
      MainViewModel.State.Initializing -> {}

      MainViewModel.State.NotAuthenticated -> {
        SignInScreen()
      }

      MainViewModel.State.Authenticated -> {
        GroceryListDetailScreen()
      }

      MainViewModel.State.RefreshFailure -> TODO()
    }
  }
}

@Preview
@Composable
private fun MainScreenNotAuthenticatedPreview() {
  MainScreen(state = MainViewModel.State.NotAuthenticated)
}
