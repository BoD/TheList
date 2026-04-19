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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.example.project.ui.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import thelist.shared.generated.resources.Res
import thelist.shared.generated.resources.signIn_email
import thelist.shared.generated.resources.signIn_password
import thelist.shared.generated.resources.signIn_signIn

@Composable
fun SignInScreen() {
  val viewModel = viewModel { SignInViewModel() }
  val state by viewModel.state.collectAsState()
  SignInScreen(
    state = state,
    onSubmit = { email, password ->
      viewModel.onSignInClick(email, password)
    },
  )
}

@Composable
fun SignInScreen(
  state: SignInViewModel.State,
  onSubmit: (email: String, password: String) -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(state) {
    if (state is SignInViewModel.State.Error) {
      snackbarHostState.showSnackbar(
        message = when (state) {
          is SignInViewModel.State.Error.InvalidCredentials -> "Invalid email or password"
          else -> "Could not sign in, please try again"
        },
        duration = SnackbarDuration.Indefinite,
      )
    }
  }
  Scaffold(
    modifier = Modifier.imePadding(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      var email: String by rememberSaveable { mutableStateOf("") }
      var password: String by rememberSaveable { mutableStateOf("") }

      // Logo
      val primaryColor = MaterialTheme.colorScheme.primary
      val tertiaryColor = MaterialTheme.colorScheme.tertiary
      val brush = remember { Brush.linearGradient(listOf(primaryColor, tertiaryColor)) }
      Text(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .rotate(-5F),
        style = MaterialTheme.typography.headlineLargeEmphasized.copy(
          brush = brush,
        ),
        fontSize = MaterialTheme.typography.headlineLargeEmphasized.fontSize * 1.5F,
        text = "The List",
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = {
          email = it
        },
        label = { Text(stringResource(Res.string.signIn_email)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = {
          password = it
        },
        label = { Text(stringResource(Res.string.signIn_password)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = state !is SignInViewModel.State.Loading,
        onClick = {
          onSubmit(email, password)
        },
      ) {
        if (state is SignInViewModel.State.Loading) {
          CircularProgressIndicator()
        } else {
          Text(stringResource(Res.string.signIn_signIn))
        }
      }
    }
  }
}

@Preview
@Composable
fun SignInScreenPreviewIdle() {
  SignInScreen(
    state = SignInViewModel.State.Idle,
    onSubmit = { _, _ -> },
  )
}

@Preview
@Composable
fun SignInScreenPreviewLoading() {
  SignInScreen(
    state = SignInViewModel.State.Loading,
    onSubmit = { _, _ -> },
  )
}

@Preview
@Composable
fun SignInScreenPreviewError() {
  SignInScreen(
    state = SignInViewModel.State.Error.InvalidCredentials,
    onSubmit = { _, _ -> },
  )
}
