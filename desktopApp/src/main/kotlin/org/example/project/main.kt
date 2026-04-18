package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
  System.setProperty("apple.awt.UIElement", "false")
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "KotlinProject",
    ) {
      App()
    }
  }
}
