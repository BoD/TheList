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

package org.jraf.thelist.ui.grocerylist.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GroceryListListScreen(
  onGroceryListClick: (groceryListId: String) -> Unit,
) {
  val viewModel = viewModel { GroceryListListViewModel() }
  val state by viewModel.state.collectAsState()
  GroceryListListScreen(state = state, onGroceryListClick = onGroceryListClick)
}

@Composable
fun GroceryListListScreen(
  state: GroceryListListViewModel.State,
  onGroceryListClick: (groceryListId: String) -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (state) {
      is GroceryListListViewModel.State.Loading -> {
        Text("Loading...")
      }

      is GroceryListListViewModel.State.Error -> {
        Text("Error: ${state.error.message}")
      }

      is GroceryListListViewModel.State.Success -> {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(state.groceryListList, key = { it.id }) { groceryList ->
            ListItem(
              modifier = Modifier.fillMaxWidth(),
              headlineContent = { Text(groceryList.name) },
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun GroceryListListScreenPreview() {
  GroceryListListScreen(
    state = GroceryListListViewModel.State.Success(
      listOf(
        GroceryListListViewModel.GroceryList(
          id = "1",
          name = "Groceries for week 1",
        ),
        GroceryListListViewModel.GroceryList(
          id = "2",
          name = "Groceries for week 2",
        ),
      ),
    ),
    onGroceryListClick = {},
  )
}
