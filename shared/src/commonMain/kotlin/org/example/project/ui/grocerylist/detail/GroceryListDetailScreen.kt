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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.backend.GroceryRepository.Groceries
import org.example.project.backend.GroceryRepository.GroceryItem
import org.example.project.backend.GroceryRepository.GroceryListEntry
import org.example.project.ui.grocerylist.detail.GroceryListDetailViewModel.State
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import thelist.shared.generated.resources.Res
import thelist.shared.generated.resources.app_name
import thelist.shared.generated.resources.groceryListDetail_addNewItem
import thelist.shared.generated.resources.groceryListDetail_availableItems
import thelist.shared.generated.resources.groceryListDetail_empty
import thelist.shared.generated.resources.groceryListDetail_logout
import thelist.shared.generated.resources.groceryListDetail_search
import thelist.shared.generated.resources.logout_24px

@Composable
fun GroceryListDetailScreen() {
  val viewModel = viewModel { GroceryListDetailViewModel() }
  val state by viewModel.state.collectAsState()
  GroceryListDetailScreen(
    state = state,
    onSignOutClick = viewModel::onSignOutClick,
    onGroceryListEntryClick = viewModel::onGroceryListEntryClick,
    onGroceryItemClick = viewModel::onGroceryItemClick,
    onFilterChange = viewModel::onFilterChange,
    onNewItemClick = viewModel::onNewItemClick,
  )
}

@Composable
private fun GroceryListDetailScreen(
  state: State,
  onSignOutClick: () -> Unit,
  onGroceryListEntryClick: (GroceryListEntry) -> Unit,
  onGroceryItemClick: (GroceryItem) -> Unit,
  onFilterChange: (String) -> Unit,
  onNewItemClick: (String) -> Unit,
) {
  Scaffold(
    modifier = Modifier.imePadding(),
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
      Crossfade(state is State.Loading) { isLoading ->
        if (isLoading) {
          Loading()
        } else {
          when (state) {
            // Cannot happen, handled by Crossfade
            State.Loading -> {}

            is State.Error -> {
              // TODO
              Text("Error: ${state.error.message}")
            }

            is State.Success -> {
              if (state.groceries.itemsInList.isEmpty() && state.groceries.availableItems.isEmpty()) {
                Empty()
              } else {
                GroceryGridWithSearch(
                  groceries = state.groceries,
                  newItem = state.newItem,
                  filter = state.filter,
                  onGroceryListEntryClick = onGroceryListEntryClick,
                  onGroceryItemClick = onGroceryItemClick,
                  onFilterChange = onFilterChange,
                  onNewItemClick = onNewItemClick,
                )
              }
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
private fun GroceryGridWithSearch(
  groceries: Groceries,
  newItem: String?,
  filter: String,
  onGroceryListEntryClick: (GroceryListEntry) -> Unit,
  onGroceryItemClick: (GroceryItem) -> Unit,
  onFilterChange: (String) -> Unit,
  onNewItemClick: (String) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
  ) {
    GroceryGrid(
      modifier = Modifier
        .weight(1F)
        .fillMaxWidth(),
      groceries = groceries,
      newItem = newItem,
      onGroceryListEntryClick = onGroceryListEntryClick,
      onGroceryItemClick = onGroceryItemClick,
      onNewItemClick = onNewItemClick,
    )

    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      placeholder = { Text(stringResource(Res.string.groceryListDetail_search)) },
      value = filter,
      onValueChange = { onFilterChange(it) },
    )
  }
}

@Composable
private fun GroceryGrid(
  modifier: Modifier,
  groceries: Groceries,
  newItem: String?,
  onGroceryListEntryClick: (GroceryListEntry) -> Unit,
  onGroceryItemClick: (GroceryItem) -> Unit,
  onNewItemClick: (String) -> Unit,
) {
  LazyVerticalGrid(
    modifier = modifier,
    columns = GridCells.Adaptive(minSize = 96.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    items(groceries.itemsInList, key = { it.groceryListId + "/" + it.groceryItem.id }) { groceryListEntry ->
      GroceryListEntry(groceryListEntry = groceryListEntry, onClick = { onGroceryListEntryClick(groceryListEntry) })
    }
    item(key = "Separator", span = { GridItemSpan(maxLineSpan) }) {
      Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(
          if (newItem != null && groceries.availableItems.isEmpty()) {
            Res.string.groceryListDetail_addNewItem
          } else {
            Res.string.groceryListDetail_availableItems
          },
        ),
        style = MaterialTheme.typography.headlineSmall,
      )
    }
    items(groceries.availableItems, key = { it.id }) { groceryItem ->
      GroceryItem(groceryItem = groceryItem, onClick = { onGroceryItemClick(groceryItem) })
    }
    if (newItem != null) {
      item(key = "NewItem") {
        GridItem(
          text = newItem,
          onClick = { onNewItemClick(newItem) },
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
      }
    }
  }
}

@Composable
private fun GroceryListEntry(
  groceryListEntry: GroceryListEntry,
  onClick: () -> Unit,
) {
  GridItem(
    text = groceryListEntry.groceryItem.name,
    onClick = onClick,
    containerColor = MaterialTheme.colorScheme.primaryContainer,
  )
}

@Composable
private fun GroceryItem(
  groceryItem: GroceryItem,
  onClick: () -> Unit,
) {
  GridItem(
    text = groceryItem.name,
    onClick = onClick,
    containerColor = Color.Unspecified,
  )
}

@Composable
private fun GridItem(
  text: String,
  onClick: () -> Unit,
  containerColor: Color,
) {
  Card(
    modifier = Modifier.aspectRatio(1f),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val isSingleWord = text.trim().none(Char::isWhitespace)
      Text(
        style = MaterialTheme.typography.headlineSmall,
        text = text.replace(' ', '\n'),
        softWrap = !isSingleWord,
        // Commented for now due to
        // https://youtrack.jetbrains.com/projects/CMP/issues/CMP-9220/Support-TextAutoSize
        // overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.headlineSmall.fontSize),
      )
    }
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
      Groceries(
        itemsInList = listOf(
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "1",
              name = "Eggs",
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "2",
              name = "Milk",
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "3",
              name = "Bread",
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "4",
              name = "TV Dinner BoD",
            ),
          ),
        ),
        availableItems = listOf(
          GroceryItem(
            id = "5",
            name = "Butter",
          ),
          GroceryItem(
            id = "6",
            name = "Cheese",
          ),
          GroceryItem(
            id = "7",
            name = "Yogurt",
          ),
          GroceryItem(
            id = "8",
            name = "Ice Cream",
          ),
        ),
      ),
      newItem = "Chocolate",
      filter = "",
    ),
    onSignOutClick = {},
    onGroceryListEntryClick = {},
    onGroceryItemClick = {},
    onFilterChange = {},
    onNewItemClick = {},
  )
}

@Preview
@Composable
private fun LoadingGroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = State.Loading,
    onSignOutClick = {},
    onGroceryListEntryClick = {},
    onGroceryItemClick = {},
    onFilterChange = {},
    onNewItemClick = {},
  )
}

@Preview
@Composable
private fun EmptyGroceryListDetailScreenPreview() {
  GroceryListDetailScreen(
    state = State.Success(groceries = Groceries(emptyList(), emptyList()), newItem = null, filter = ""),
    onSignOutClick = {},
    onGroceryListEntryClick = {},
    onGroceryItemClick = {},
    onFilterChange = {},
    onNewItemClick = {},
  )
}
