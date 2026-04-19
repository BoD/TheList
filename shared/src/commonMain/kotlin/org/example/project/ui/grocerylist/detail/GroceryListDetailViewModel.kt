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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.backend.supabaseClient


private const val THE_LIST_ID = "5100ed05-b32f-4609-b1b3-a5e297e4141d"

class GroceryListDetailViewModel : ViewModel() {
  sealed interface State {
    object Loading : State
    data class Success(val groceryItemList: List<GroceryItem>) : State
    data class Error(val error: Throwable) : State
  }

  @Serializable
  data class GroceryItem(
    @SerialName("grocery_items")
    val groceryItems: GroceryItems,
  ) {
    @Serializable
    data class GroceryItems(
      val id: String,
      val name: String,
    )
  }

  val state: StateFlow<State> = flow {
    runCatching {
      supabaseClient
        .from("grocery_list_contents")
        .select(Columns.raw("grocery_items(id,name)")) {
          filter {
            eq("grocery_list_id", THE_LIST_ID)
          }
        }
        .decodeList<GroceryItem>()
    }.fold(
      onSuccess = { groceryItemList ->
        emit(State.Success(groceryItemList))
      },
      onFailure = { error ->
        emit(State.Error(error))
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
}
