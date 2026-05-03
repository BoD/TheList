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

package org.jraf.thelist.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jraf.thelist.backend.supabaseClient

class MainViewModel : ViewModel() {
  sealed interface State {
    object Initializing : State
    object NotAuthenticated : State
    object Authenticated : State
    object RefreshFailure : State
  }

  val state: StateFlow<State> = supabaseClient.auth.sessionStatus.map { sessionStatus ->
    when (sessionStatus) {
      is SessionStatus.Initializing -> State.Initializing
      is SessionStatus.Authenticated -> State.Authenticated
      is SessionStatus.NotAuthenticated -> State.NotAuthenticated
      is SessionStatus.RefreshFailure -> State.RefreshFailure
    }
  }
    .stateIn(
      viewModelScope,
      SharingStarted.Lazily,
      State.Initializing,
    )
}
