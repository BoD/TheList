package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.ui.main.MainScreen
import org.example.project.ui.platform.NoOpPlatform

fun main() {
  System.setProperty("apple.awt.UIElement", "false")
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "The List",
    ) {
      MainScreen(NoOpPlatform)
    }
  }
}
