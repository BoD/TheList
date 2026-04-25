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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.ui.grocerylist.detail.GroceryListDetailViewModel.GroceryItem
import org.example.project.ui.grocerylist.detail.GroceryListDetailViewModel.State
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import thelist.shared.generated.resources.Res
import thelist.shared.generated.resources.app_name
import thelist.shared.generated.resources.groceryListDetail_empty
import thelist.shared.generated.resources.groceryListDetail_logout
import thelist.shared.generated.resources.logout_24px

@Composable
fun GroceryListDetailScreen() {
  val viewModel = viewModel { GroceryListDetailViewModel() }
  val state by viewModel.state.collectAsState()
  GroceryListDetailScreen(
    state = state,
    onSignOutClick = viewModel::onSignOutClick,
    onGroceryItemClick = viewModel::onGroceryItemClick,
  )
}

@Composable
private fun GroceryListDetailScreen(
  state: State,
  onSignOutClick: () -> Unit,
  onGroceryItemClick: (GroceryItem) -> Unit,
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
      Crossfade(state) { state ->
        when (state) {
          State.Loading -> {
            Loading()
          }

          is State.Error -> {
            // TODO
            Text("Error: ${state.error.message}")
          }

          is State.Success -> {
            if (state.groceryItemList.isEmpty()) {
              Empty()
            } else {
              GroceryGrid(groceryItems = state.groceryItemList, onGroceryItemClick = onGroceryItemClick)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Empty() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = stringResource(Res.string.groceryListDetail_empty),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun GroceryGrid(
  groceryItems: List<GroceryItem>,
  onGroceryItemClick: (GroceryItem) -> Unit,
) {
  LazyVerticalGrid(
    modifier = Modifier.fillMaxSize(),
    columns = GridCells.Adaptive(minSize = 128.dp),
  ) {
    items(groceryItems, key = { it.groceryItem.id }) { groceryItem ->
      GroceryItem(groceryItem = groceryItem, onClick = { onGroceryItemClick(groceryItem) })
    }
  }
}

@Composable
private fun GroceryItem(
  groceryItem: GroceryItem,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .padding(16.dp)
      .height(128.dp)
      .clickable(onClick = onClick),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      style = MaterialTheme.typography.headlineMedium,
      text = groceryItem.groceryItem.name,
    )
  }
}

@Composable
private fun Loading() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator()
  }
}

@Preview
@Composable
private fun SuccessGroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = State.Success(
      listOf(
        GroceryItem(
          groceryListId = "1",
          groceryItem = GroceryItem.GroceryItem(
            id = "1",
            name = "Eggs",
          ),
        ),
        GroceryItem(
          groceryListId = "1",
          groceryItem = GroceryItem.GroceryItem(
            id = "2",
            name = "Milk",
          ),
        ),
      ),
    ),
    onSignOutClick = {},
    onGroceryItemClick = {},
  )
}

@Preview
@Composable
private fun LoadingGroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = State.Loading,
    onSignOutClick = {},
    onGroceryItemClick = {},
  )
}

@Preview
@Composable
private fun EmptyGroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = State.Success(emptyList()),
    onSignOutClick = {},
    onGroceryItemClick = {},
  )
}
