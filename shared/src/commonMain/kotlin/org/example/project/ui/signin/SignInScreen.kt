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

package org.example.project.ui.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignInScreen() {
  val viewModel = viewModel { SignInViewModel() }
  SignInScreen(
    onSubmit = { email, password ->
      viewModel.onSignInClick(email, password)
    },
  )
}

@Composable
fun SignInScreen(onSubmit: (email: String, password: String) -> Unit) {
  var email: String by remember { mutableStateOf("") }
  var password: String by remember { mutableStateOf("") }
  Column(
    modifier = Modifier
      .padding(16.dp)
      .fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = email,
      onValueChange = {
        email = it
      },
      label = { Text("Email") },
    )

    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = password,
      onValueChange = {
        password = it
      },
      label = { Text("Password") },
    )

    Button(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        onSubmit(email, password)
      },
    ) {
      Text("Sign In")
    }
  }
}

@Preview
@Composable
fun SignInScreenPreview() {
  SignInScreen(onSubmit = { _, _ -> })
}
