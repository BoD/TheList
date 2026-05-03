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

package org.jraf.thelist.ui.grocerylist.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jraf.klibnanolog.logd
import org.jraf.thelist.backend.GroceryRepository
import org.jraf.thelist.backend.GroceryRepository.Groceries
import org.jraf.thelist.backend.GroceryRepository.GroceryItem
import org.jraf.thelist.backend.GroceryRepository.GroceryListEntry
import org.jraf.thelist.backend.supabaseClient
import org.jraf.thelist.util.Signal

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

  private val reload = Signal()
  private val filter = MutableStateFlow("")

  private val groceries = MutableStateFlow<Result<Groceries>?>(null)

  private val groceriesFromRepository: Flow<Result<Groceries>> = merge(
    reload,
    groceryRepository.observeGroceryListChanged(),
  )
    .map {
      logd("Reloading groceries")
      groceryRepository.getGroceries()
    }

  val state: StateFlow<State> = combine(
    groceries.filterNotNull(),
    filter,
  ) { groceries, filter ->
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

  val hideKeyboard = Signal()

  init {
    viewModelScope.launch {
      groceriesFromRepository.collect { groceries ->
        this@GroceryListDetailViewModel.groceries.value = groceries
      }
    }
  }

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
    if (groceryListContentItem.groceryListId == "temporary_list_id") {
      // Temporary item, we can't do much, so let's just ignore this click
      return
    }
    filter.value = ""
    hideKeyboard()
    groceries.value = groceries.value?.mapCatching { groceries ->
      Groceries(
        itemsInList = groceries.itemsInList.filterNot { it.groceryItem.id == groceryListContentItem.groceryItem.id },
        availableItems = (groceries.availableItems + groceryListContentItem.groceryItem),
      )
    }
    viewModelScope.launch {
      groceryRepository.removeItemFromList(groceryListContentItem)
    }
  }

  fun onGroceryItemClick(groceryItem: GroceryItem) {
    filter.value = ""
    hideKeyboard()
    groceries.value = groceries.value?.mapCatching { groceries ->
      Groceries(
        itemsInList = groceries.itemsInList + GroceryListEntry(
          "temporary_list_id",
          groceryItem.copy(addedCount = groceryItem.addedCount + 1),
        ),
        availableItems = groceries.availableItems.filterNot { it.id == groceryItem.id },
      )
    }
    viewModelScope.launch {
      groceryRepository.addItemToList(groceryItem)
    }
  }

  fun onNewItemClick(newItem: String) {
    val newItem = newItem.trim()
    filter.value = ""
    hideKeyboard()
    groceries.value = groceries.value?.mapCatching { groceries ->
      Groceries(
        itemsInList = groceries.itemsInList + GroceryListEntry(
          "temporary_list_id",
          GroceryItem(
            id = "temporary_item_id",
            name = newItem,
            addedCount = 1,
          ),
        ),
        availableItems = groceries.availableItems,
      )
    }
    viewModelScope.launch {
      groceryRepository.createAndAddItemToList(name = newItem)
    }
  }

  fun onFilterChange(filter: String) {
    this.filter.value = filter
  }

  private fun Groceries.filtered(filter: String): Groceries {
    val filter = filter.trim()
    if (filter.isBlank()) return this
    return Groceries(
      itemsInList = itemsInList,
      availableItems = availableItems.filter { it.name.contains(filter, ignoreCase = true) },
    )
  }
}
