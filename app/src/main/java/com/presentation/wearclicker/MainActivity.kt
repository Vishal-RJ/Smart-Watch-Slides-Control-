package com.presentation.wearclicker

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.presentation.wearclicker.ui.ClickerScreen
import com.presentation.wearclicker.ui.PresentationViewModel
import com.presentation.wearclicker.ui.theme.PresentationClickerTheme

/**
 * Main Wear OS Activity.
 * Keeps the screen awake during presentations and hosts the ClickerScreen Composable.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: PresentationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on during presentations so the watch doesn't go to sleep
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            PresentationClickerTheme {
                ClickerScreen(viewModel = viewModel)
            }
        }
    }
}
