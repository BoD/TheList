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

package org.example.project.ui.grocerylist.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import thelist.shared.generated.resources.Res
import thelist.shared.generated.resources.app_name
import thelist.shared.generated.resources.groceryListDetail_logout
import thelist.shared.generated.resources.logout_24px

@Composable
fun GroceryListDetailScreen() {
  val viewModel = viewModel { GroceryListDetailViewModel() }
  val state by viewModel.state.collectAsState()
  GroceryListDetailScreen(
    state = state,
    onSignOutClick = viewModel::onSignOutClick,
  )
}

@Composable
private fun GroceryListDetailScreen(
  state: GroceryListDetailViewModel.State,
  onSignOutClick: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(Res.string.app_name)) },
        actions = {
          IconButton(onClick = { onSignOutClick() }) {
            Icon(
              painter = painterResource(Res.drawable.logout_24px),
              contentDescription = stringResource(Res.string.groceryListDetail_logout),
            )
          }
        },
      )
    },
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .padding(paddingValues),
    ) {
      when (state) {
        is GroceryListDetailViewModel.State.Loading -> {
          Text("Loading...")
        }

        is GroceryListDetailViewModel.State.Error -> {
          Text("Error: ${state.error.message}")
        }

        is GroceryListDetailViewModel.State.Success -> {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.groceryItemList, key = { it.groceryItems.id }) { groceryList ->
              ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text(groceryList.groceryItems.name) },
              )
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun GroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = GroceryListDetailViewModel.State.Success(
      listOf(
        GroceryListDetailViewModel.GroceryItem(
          groceryItems = GroceryListDetailViewModel.GroceryItem.GroceryItems(
            id = "1",
            name = "Eggs",
          ),
        ),
        GroceryListDetailViewModel.GroceryItem(
          groceryItems = GroceryListDetailViewModel.GroceryItem.GroceryItems(
            id = "2",
            name = "Milk",
          ),
        ),
      ),
    ),
    onSignOutClick = {},
  )
}
