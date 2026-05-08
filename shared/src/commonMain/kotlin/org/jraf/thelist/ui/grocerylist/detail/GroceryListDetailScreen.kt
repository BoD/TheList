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

package org.jraf.thelist.ui.grocerylist.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jraf.thelist.backend.GroceryRepository.Groceries
import org.jraf.thelist.backend.GroceryRepository.GroceryItem
import org.jraf.thelist.backend.GroceryRepository.GroceryListEntry
import org.jraf.thelist.ui.grocerylist.detail.GroceryListDetailViewModel.State
import org.jraf.thelist.ui.platform.Platform
import org.jraf.thelist.util.Signal
import org.jraf.thelist.util.capitalize
import org.jraf.thelist.util.capitalizeWords
import org.jraf.thelist.util.splitFirstWord
import thelist.shared.generated.resources.Res
import thelist.shared.generated.resources.app_name
import thelist.shared.generated.resources.groceryListDetail_addNewItem
import thelist.shared.generated.resources.groceryListDetail_availableItems
import thelist.shared.generated.resources.groceryListDetail_empty
import thelist.shared.generated.resources.groceryListDetail_logout
import thelist.shared.generated.resources.groceryListDetail_more
import thelist.shared.generated.resources.groceryListDetail_search
import thelist.shared.generated.resources.more_vert_24px
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
    modifier = Modifier
      .imePadding(),
    // Uncomment to show/hide keyboard depending on scroll, but it's a bit buggy
//      .imeNestedScroll(),
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
          var expanded by remember { mutableStateOf(false) }
          TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(stringResource(Res.string.groceryListDetail_more)) } },
            state = rememberTooltipState(),
          ) {
            IconButton(onClick = { expanded = !expanded }) {
              Icon(
                painter = painterResource(Res.drawable.more_vert_24px),
                contentDescription = stringResource(Res.string.groceryListDetail_more),
              )
            }
          }
          DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(Res.string.groceryListDetail_logout)) },
              onClick = { onSignOutClick() },
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

@OptIn(ExperimentalFoundationApi::class)
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
  val gridState = rememberLazyGridState(cacheWindow = LazyLayoutCacheWindow(100f, 100f))
  val imeVisible = isImeVisible()
  LaunchedEffect(imeVisible, filter, groceries) {
    if (imeVisible) {
      delay(225)
      gridState.animateScrollToItem(groceries.itemsInList.size + groceries.availableItems.size + if (newItem != null) 1 else 0)
    } else {
      gridState.animateScrollToItem(0)
    }
  }
  LazyVerticalGrid(
    modifier = modifier,
    state = gridState,
    columns = GridCells.Adaptive(minSize = 96.dp),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Items in the list
    if (groceries.itemsInList.isEmpty()) {
      item(key = "Empty", span = { GridItemSpan(maxLineSpan) }, contentType = { 0 }) {
        Text(
          modifier = Modifier.padding(vertical = 8.dp).animateItem(),
          text = stringResource(Res.string.groceryListDetail_empty),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,

          )
      }
    } else {
      items(groceries.itemsInList, key = { it.groceryItem.name }, contentType = { 1 }) { groceryListEntry ->
        GroceryListEntry(groceryListEntry = groceryListEntry, onClick = { onGroceryListEntryClick(groceryListEntry) })
      }
    }

    // Available items
    if (groceries.availableItems.isNotEmpty()) {
      item(key = "Separator", span = { GridItemSpan(maxLineSpan) }, contentType = { 2 }) {
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
      items(groceries.availableItems, key = { it.name }, contentType = { 3 }) { groceryItem ->
        GroceryItem(groceryItem = groceryItem, onClick = { onGroceryItemClick(groceryItem) })
      }
    }

    // Create new item
    if (newItem != null) {
      item(key = newItem, contentType = { 4 }) {
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
        .padding(8.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val (firstWord, remainingWords) = text.splitFirstWord()
      Text(
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        text = firstWord.capitalize(),
        softWrap = false,
        // Commented for now due to
        // https://youtrack.jetbrains.com/projects/CMP/issues/CMP-9220/Support-TextAutoSize
        // overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.headlineSmall.fontSize),
      )
      if (remainingWords != null) {
        Text(
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
          text = remainingWords.capitalizeWords(),
          softWrap = false,
          autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.headlineSmall.fontSize),
        )
      }
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
