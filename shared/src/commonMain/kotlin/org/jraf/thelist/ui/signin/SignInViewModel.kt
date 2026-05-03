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

package org.jraf.thelist.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jraf.thelist.backend.supabaseClient

class SignInViewModel : ViewModel() {
  sealed interface State {
    object Idle : State
    sealed interface Error : State {
      object InvalidCredentials : Error
      data class Unknown(val error: Throwable) : Error
    }

    object Loading : State
  }

  val state: StateFlow<State> field = MutableStateFlow<State>(State.Idle)

  fun onSignInClick(email: String, password: String) {
    state.value = State.Loading
    viewModelScope.launch {
      runCatching {
        supabaseClient.auth.signInWith(Email) {
          this.email = email
          this.password = password
        }
      }.fold(
        onSuccess = {
          state.value = State.Idle
        },
        onFailure = { error ->
          state.value = when (error) {
            is AuthRestException -> State.Error.InvalidCredentials
            else -> State.Error.Unknown(error)
          }
        },
      )
    }
  }
}
