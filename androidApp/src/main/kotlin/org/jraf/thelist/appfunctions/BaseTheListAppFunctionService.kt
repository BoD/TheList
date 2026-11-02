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

package org.jraf.thelist.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import org.jraf.klibnanolog.logd
import org.jraf.thelist.TheListApplication
import org.jraf.thelist.data.GroceryRepository

@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "TheListAppFunctionService",
  appFunctionXmlFileName = "thelist_app_function_service",
)
@HasMemberInjections
abstract class BaseTheListAppFunctionService : AppFunctionService() {
  @Inject
  private lateinit var groceryRepository: GroceryRepository

  override fun onCreate() {
    (application as TheListApplication).appGraph.inject(this)
    super.onCreate()
  }

  /**
   * Lists items currently in the list.
   *
   * @return Items currently in the list.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun listItemsInTheList(): List<String> {
    logd("listItemsInTheList")
    return groceryRepository.getGroceries().getOrNull()?.let {
      it.itemsInList.map { item -> item.groceryItem.name }
    } ?: emptyList()
  }

  /**
   * Adds an item to the list.
   *
   * @param itemName The name of the item to add.
   * @return true if the item was added, false if it was already in the list.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun addItemToTheList(itemName: String): Boolean {
    logd("addItemToTheList: $itemName")
    return groceryRepository.addItemByName(itemName)
  }

  /**
   * Removes an item from the list.
   *
   * @param itemName The name of the item to remove.
   * @return true if the item was removed, false if it was not in the list.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun removeItemFromTheList(itemName: String): Boolean {
    logd("removeItemFromTheList: $itemName")
    return groceryRepository.removeItemByName(itemName)
  }
}
