package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.example.project.ui.main.MainScreen
import org.example.project.ui.platform.NoOpPlatform
import org.example.project.ui.platform.Platform

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      MainScreen(
        object : Platform {
          override fun hideKeyboard() {
            WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
          }
        },
      )
    }
  }
}

@Preview
@Composable
fun MainScreenPreview() {
  MainScreen(NoOpPlatform)
}
