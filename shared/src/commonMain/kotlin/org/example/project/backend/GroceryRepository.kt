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

package org.example.project.backend

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val THE_LIST_ID = "5100ed05-b32f-4609-b1b3-a5e297e4141d"

class GroceryRepository {
  @Serializable
  data class GroceryListEntry(
    @SerialName("grocery_list_id")
    val groceryListId: String,

    @SerialName("grocery_item")
    val groceryItem: GroceryItem,
  )

  @Serializable
  data class GroceryItem(
    val id: String,
    val name: String,
    @SerialName("added_count")
    val addedCount: Int,
  )

  class Groceries(
    val itemsInList: List<GroceryListEntry>,
    availableItems: List<GroceryItem>,
  ) {
    val availableItems: List<GroceryItem> = availableItems
      .sortedBy { it.name.lowercase() }
      .sortedByDescending { it.addedCount }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Groceries) return false
      if (itemsInList != other.itemsInList) return false
      if (availableItems != other.availableItems) return false
      return true
    }

    override fun hashCode(): Int {
      var result = itemsInList.hashCode()
      result = 31 * result + availableItems.hashCode()
      return result
    }
  }

  suspend fun getGroceries(): Result<Groceries> = runCatching {
    // TODO: Could this be a view, so we make only 1 call instead of 2?

    coroutineScope {
      val groceryListEntries = async {
        supabaseClient
          .from("grocery_list_entry")
          .select(Columns.raw("grocery_list_id, grocery_item(id, name, added_count)")) {
            filter {
              eq("grocery_list_id", THE_LIST_ID)
            }
            order(column = "created_at", order = Order.ASCENDING)
          }
          .decodeList<GroceryListEntry>()
      }

      val availableGroceryItems = async {
        supabaseClient
          .from("grocery_item")
          .select(Columns.list("id", "name", "added_count")) {
            order("added_count", Order.DESCENDING)
            order("name", Order.ASCENDING)
          }
          .decodeList<GroceryItem>()
      }

      val itemsInList = groceryListEntries.await()
      Groceries(
        itemsInList = itemsInList,
        availableItems = availableGroceryItems.await()
          // Filter out already added grocery items from the available grocery items list
          .filter { groceryItem ->
            itemsInList.none { it.groceryItem.id == groceryItem.id }
          },
      )
    }
  }

  @OptIn(SupabaseExperimental::class)
  fun observeGroceryListChanged(): Flow<Unit> {
    @Serializable
    data class GroceryListEntry(
      val grocery_list_id: String,
    )

    val groceryListEntryFlow = supabaseClient
      .from("grocery_list_entry")
      .selectAsFlow(GroceryListEntry::grocery_list_id, filter = FilterOperation("grocery_list_id", FilterOperator.EQ, THE_LIST_ID))
      .drop(1) // Drop the initial value emitted by selectAsFlow, as we only want to react to changes

    return groceryListEntryFlow.map { }
  }

  suspend fun removeItemFromList(groceryListContentItem: GroceryListEntry) {
    supabaseClient
      .from("grocery_list_entry")
      .delete {
        filter {
          eq("grocery_list_id", groceryListContentItem.groceryListId)
          eq("grocery_item_id", groceryListContentItem.groceryItem.id)
        }
      }
  }

  suspend fun addItemToList(groceryItem: GroceryItem) {
    supabaseClient
      .from("grocery_list_entry")
      .insert(
        buildJsonObject {
          put("grocery_list_id", THE_LIST_ID)
          put("grocery_item_id", groceryItem.id)
        },
      )
  }

  suspend fun createAndAddItemToList(name: String) {
    supabaseClient.postgrest.rpc(
      "create_and_add_item_to_list",
      buildJsonObject {
        put("p_grocery_list_id", THE_LIST_ID)
        put("p_name", name)
        put("p_quantity", 1)
      },
    )
  }
}
