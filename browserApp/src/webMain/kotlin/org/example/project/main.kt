package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.ui.main.MainScreen

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  ComposeViewport {
    MainScreen()
  }
}
