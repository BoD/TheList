package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.backend.supabaseClient
import org.example.project.ui.signin.SignInScreen

@Composable
@Preview
fun App() {
  MaterialTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.primaryContainer)
        .safeContentPadding()
        .fillMaxSize(),
    ) {
      val sessionStatus by supabaseClient.auth.sessionStatus.collectAsState()
      when (sessionStatus) {
        SessionStatus.Initializing -> {}
        is SessionStatus.NotAuthenticated -> {
          SignInScreen()
        }

        is SessionStatus.Authenticated -> {
          GroceryListListScreen()
        }

        is SessionStatus.RefreshFailure -> TODO()
      }
    }
  }
}

@Composable
fun GroceryListListScreen() {
  Text("Hello, you are signed in!")
}
