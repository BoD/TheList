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

package org.example.project.ui.grocerylist.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.project.backend.GroceryRepository
import org.example.project.backend.GroceryRepository.Groceries
import org.example.project.backend.GroceryRepository.GroceryItem
import org.example.project.backend.GroceryRepository.GroceryListEntry
import org.example.project.backend.supabaseClient

class GroceryListDetailViewModel : ViewModel() {
  sealed interface State {
    object Loading : State
    data class Success(
      val groceries: Groceries,
      val filter: String,
      val newItem: String?,
    ) : State

    data class Error(val error: Throwable) : State
  }

  private val groceryRepository = GroceryRepository()

  private val reload = MutableSharedFlow<Unit>()
  private val filter = MutableStateFlow("")
  private val groceries: Flow<Result<Groceries>> = reload
    .onStart { emit(Unit) }
    .map {
      groceryRepository.getGroceries()
    }

  val state: StateFlow<State> = combine(groceries, filter) { groceries, filter ->
    groceries.fold(
      onSuccess = { groceries ->
        val filteredGroceries = groceries.filtered(filter)
        State.Success(
          groceries = filteredGroceries,
          filter = filter,
          newItem = getNewItemFromFilter(filter, filteredGroceries),
        )
      },
      onFailure = { error ->
        State.Error(error)
      },
    )
  }
    .stateIn(
      viewModelScope,
      SharingStarted.Lazily,
      State.Loading,
    )

  private fun getNewItemFromFilter(filter: String, groceries: Groceries): String? {
    val filter = filter.trim()
    if (filter.isBlank()) return null
    if (
      (groceries.availableItems.map { it.name } + groceries.itemsInList.map { it.groceryItem.name })
        .any { it.equals(filter, ignoreCase = true) }
    ) return null
    return filter
  }

  fun onSignOutClick() {
    viewModelScope.launch {
      supabaseClient.auth.signOut()
    }
  }

  fun onGroceryListEntryClick(groceryListContentItem: GroceryListEntry) {
    filter.value = ""
    viewModelScope.launch {
      groceryRepository.removeItemFromList(groceryListContentItem)
      reload.emit(Unit)
    }
  }

  fun onGroceryItemClick(groceryItem: GroceryItem) {
    filter.value = ""
    viewModelScope.launch {
      groceryRepository.addItemToList(groceryItem)
      reload.emit(Unit)
    }
  }

  fun onFilterChange(filter: String) {
    this.filter.value = filter
  }

  fun onNewItemClick(newItem: String) {
    filter.value = ""
    viewModelScope.launch {
      groceryRepository.createAndAddItemToList(name = newItem)
      reload.emit(Unit)
    }
  }

  private fun Groceries.filtered(filter: String): Groceries {
    val filter = filter.trim()
    if (filter.isBlank()) return this
    return copy(
      availableItems = availableItems.filter { it.name.contains(filter, ignoreCase = true) },
    )
  }
}
