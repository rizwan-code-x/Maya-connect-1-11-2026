package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MayaApp
import com.example.ui.viewmodel.MayaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MayaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure full GPU hardware acceleration across Activity Window & WebViews
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        enableHighRefreshRate()
        enableEdgeToEdge()
        setContent {
            MayaApp(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        enableHighRefreshRate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableHighRefreshRate()
        }
    }

    /**
     * Unlocks maximum supported display refresh rate (90Hz / 120Hz / 144Hz)
     * so both native Compose UI and In-App WebViews scroll at peak smoothness.
     */
    @Suppress("DEPRECATION")
    private fun enableHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    windowManager.defaultDisplay
                } ?: return

                val supportedModes = currentDisplay.supportedModes
                val activeMode = currentDisplay.mode
                // Pick the mode matching current resolution with the highest refresh rate (e.g. 120Hz / 144Hz)
                val bestMode = supportedModes
                    .filter {
                        it.physicalWidth == activeMode.physicalWidth &&
                                it.physicalHeight == activeMode.physicalHeight
                    }
                    .maxByOrNull { it.refreshRate }
                    ?: supportedModes.maxByOrNull { it.refreshRate }

                if (bestMode != null) {
                    val params = window.attributes
                    if (params.preferredDisplayModeId != bestMode.modeId ||
                        params.preferredRefreshRate != bestMode.refreshRate
                    ) {
                        params.preferredDisplayModeId = bestMode.modeId
                        params.preferredRefreshRate = bestMode.refreshRate
                        window.attributes = params
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}

