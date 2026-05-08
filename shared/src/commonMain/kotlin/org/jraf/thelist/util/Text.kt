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

package org.jraf.thelist.util

fun String.capitalize() = replaceFirstChar { it.titlecase() }

fun String.capitalizeWords(): String {
  return buildString {
    var capitalizeNext = true
    for (char in this@capitalizeWords) {
      if (char.isWhitespace()) {
        capitalizeNext = true
        append(char)
      } else {
        if (capitalizeNext) {
          append(char.uppercaseChar())
          capitalizeNext = false
        } else {
          append(char)
        }
      }
    }
  }
}

fun String.splitFirstWord(): Pair<String, String?> {
  val index = indexOfFirst { it.isWhitespace() }
  return if (index == -1) {
    this to null
  } else {
    substring(0, index) to substring(index + 1).trim()
  }
}
