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

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
  )

  data class Groceries(
    val itemsInList: List<GroceryListEntry>,
    val availableItems: List<GroceryItem>,
  )

  suspend fun getGroceries(): Result<Groceries> = runCatching {
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
        mapOf(
          "grocery_list_id" to THE_LIST_ID,
          "grocery_item_id" to groceryItem.id,
        ),
      )
  }
}
