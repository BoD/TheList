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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.example.project.backend.GroceryRepository.Groceries
import org.example.project.backend.GroceryRepository.GroceryItem
import org.example.project.backend.GroceryRepository.GroceryListEntry
import org.example.project.ui.grocerylist.detail.GroceryListDetailViewModel.State
import org.example.project.ui.platform.Platform
import org.example.project.util.Signal
import org.example.project.util.capitalizeWords
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
import thelist.shared.generated.resources.the_list_logo_horizontal

@Composable
fun GroceryListDetailScreen(platform: Platform) {
  val viewModel = viewModel { GroceryListDetailViewModel() }
  val hideKeyboard by viewModel.hideKeyboard.collectAsState()
  LaunchedEffect(hideKeyboard) {
    if (hideKeyboard != Signal.Initial) platform.hideKeyboard()
  }
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
        title = {
          Icon(
            modifier = Modifier.height(32.dp),
            painter = painterResource(Res.drawable.the_list_logo_horizontal),
            contentDescription = stringResource(Res.string.app_name),
            tint = MaterialTheme.colorScheme.primary,
          )
        },
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
      filter = filter,
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
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
    )
  }
}

@Composable
private fun GroceryGrid(
  modifier: Modifier,
  groceries: Groceries,
  newItem: String?,
  filter: String,
  onGroceryListEntryClick: (GroceryListEntry) -> Unit,
  onGroceryItemClick: (GroceryItem) -> Unit,
  onNewItemClick: (String) -> Unit,
) {
  val gridState = rememberLazyGridState()
  val imeVisible = isImeVisible()
  LaunchedEffect(imeVisible, filter) {
    delay(225)
    if (imeVisible) {
      gridState.animateScrollToItem(groceries.itemsInList.size + groceries.availableItems.size)
    } else {
      gridState.animateScrollToItem(0)
    }
  }
  LazyVerticalGrid(
    modifier = modifier,
    state = gridState,
    columns = GridCells.Adaptive(minSize = 96.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    items(groceries.itemsInList, key = { it.groceryItem.name }) { groceryListEntry ->
      GroceryListEntry(groceryListEntry = groceryListEntry, onClick = { onGroceryListEntryClick(groceryListEntry) })
    }
    item(key = "Separator", span = { GridItemSpan(maxLineSpan) }) {
      Text(
        modifier = Modifier.padding(vertical = 8.dp).animateItem(),
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
    items(groceries.availableItems, key = { it.name }) { groceryItem ->
      GroceryItem(groceryItem = groceryItem, onClick = { onGroceryItemClick(groceryItem) })
    }
    if (newItem != null) {
      item(key = newItem) {
        // Do not animate this one, because otherwise it 'blinks' for every typed character
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
private fun LazyGridItemScope.GroceryListEntry(
  groceryListEntry: GroceryListEntry,
  onClick: () -> Unit,
) {
  GridItem(
    modifier = Modifier.animateItem(),
    text = groceryListEntry.groceryItem.name,
    onClick = onClick,
    containerColor = MaterialTheme.colorScheme.primaryContainer,
  )
}

@Composable
private fun LazyGridItemScope.GroceryItem(
  groceryItem: GroceryItem,
  onClick: () -> Unit,
) {
  GridItem(
    modifier = Modifier.animateItem(),
    text = groceryItem.name,
    onClick = onClick,
    containerColor = Color.Unspecified,
  )
}

@Composable
private fun GridItem(
  modifier: Modifier = Modifier,
  text: String,
  onClick: () -> Unit,
  containerColor: Color,
) {
  Card(
    modifier = modifier.aspectRatio(1f),
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
        textAlign = TextAlign.Center,
        text = text.replace(' ', '\n').capitalizeWords(),
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

// WindowInsets.isImeVisible is Android-only
@Composable
private fun isImeVisible(): Boolean {
  val density = LocalDensity.current
  val ime = WindowInsets.ime
  val isImeVisible = remember(ime, density) { derivedStateOf { ime.getBottom(density) > 0 } }
  return isImeVisible.value
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
              addedCount = 4,
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "2",
              name = "Milk",
              addedCount = 4,
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "3",
              name = "Bread",
              addedCount = 4,
            ),
          ),
          GroceryListEntry(
            groceryListId = "1",
            groceryItem = GroceryItem(
              id = "4",
              name = "TV Dinner BoD",
              addedCount = 4,
            ),
          ),
        ),
        availableItems = listOf(
          GroceryItem(
            id = "5",
            name = "Butter",
            addedCount = 4,
          ),
          GroceryItem(
            id = "6",
            name = "Cheese",
            addedCount = 4,
          ),
          GroceryItem(
            id = "7",
            name = "Yogurt",
            addedCount = 4,
          ),
          GroceryItem(
            id = "8",
            name = "Ice cream",
            addedCount = 4,
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
