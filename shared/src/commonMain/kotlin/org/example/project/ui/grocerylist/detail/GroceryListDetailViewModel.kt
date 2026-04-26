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
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.backend.supabaseClient


private const val THE_LIST_ID = "5100ed05-b32f-4609-b1b3-a5e297e4141d"

class GroceryListDetailViewModel : ViewModel() {
  sealed interface State {
    object Loading : State
    data class Success(
      val groceryListEntries: List<GroceryListEntry>,
      val availableGroceryItems: List<GroceryItem>,
    ) : State

    data class Error(val error: Throwable) : State
  }

  @Serializable
  data class GroceryListEntry(
    @SerialName("grocery_list_id")
    val groceryListId: String,

    @SerialName("grocery_item")
    val groceryItem: GroceryItem,
  ) {
  }

  @Serializable
  data class GroceryItem(
    val id: String,
    val name: String,
  )

  private val reloadFlow = MutableSharedFlow<Unit>()

  val state: StateFlow<State> = reloadFlow
    .onStart { emit(Unit) }
    .map {
      runCatching {
        coroutineScope {
          val groceryListEntries = async {
            supabaseClient
              .from("grocery_list_entry")
              .select(Columns.raw("grocery_list_id, grocery_item(id,name)")) {
                filter {
                  eq("grocery_list_id", THE_LIST_ID)
                }
              }
              .decodeList<GroceryListEntry>()
          }

          val availableGroceryItems = async {
            supabaseClient
              .from("grocery_item")
              .select(Columns.list("id", "name")) {
                order("added_count", Order.DESCENDING)
              }
              .decodeList<GroceryItem>()
          }

          groceryListEntries.await() to availableGroceryItems.await()
        }
      }.fold(
        onSuccess = { (groceryListEntries, availableGroceryItems) ->
          State.Success(
            groceryListEntries = groceryListEntries,
            availableGroceryItems = availableGroceryItems
              // Filter out already added grocery items from the available grocery items list
              .filter { availableGroceryItem ->
                groceryListEntries.none { it.groceryItem.id == availableGroceryItem.id }
              },
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


  fun onSignOutClick() {
    viewModelScope.launch {
      supabaseClient.auth.signOut()
    }
  }

  fun onGroceryListEntryClick(groceryListContentItem: GroceryListEntry) {
    viewModelScope.launch {
      supabaseClient
        .from("grocery_list_entry")
        .delete {
          filter {
            eq("grocery_list_id", groceryListContentItem.groceryListId)
            eq("grocery_item_id", groceryListContentItem.groceryItem.id)
          }
        }

      reloadFlow.emit(Unit)
    }
  }

  fun onGroceryItemClick(groceryItem: GroceryItem) {
    viewModelScope.launch {
      supabaseClient
        .from("grocery_list_entry")
        .insert(
          mapOf(
            "grocery_list_id" to THE_LIST_ID,
            "grocery_item_id" to groceryItem.id,
          ),
        )

      reloadFlow.emit(Unit)
    }
  }
}
