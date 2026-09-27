package com.nighthelper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nighthelper.app.ui.NightHelperRoot
import com.nighthelper.app.ui.theme.NightHelperTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        val startRoute = intent?.getStringExtra(EXTRA_ROUTE) ?: Route.FLOW
        setContent {
            NightHelperTheme {
                NightHelperRoot(startRoute = startRoute)
            }
        }
    }

    companion object {
        const val EXTRA_ROUTE = "route"
    }
}

object Route {
    const val FLOW = "flow"
    const val GOODNIGHT = "goodnight"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
}
