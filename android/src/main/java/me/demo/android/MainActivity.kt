package me.demo.android

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.demo.common.App
import me.demo.common.CommonAppBar
import me.demo.common.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @Composable
    fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
        MaterialTheme(
            colors = if (darkTheme) darkColors(primaryVariant = Color.Black) else lightColors(),
            content = content
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.preferences = getPreferences(Context.MODE_PRIVATE)
        viewModel.getViewParams()
        setContent {
            AppTheme(viewModel.darkMode) {
                val config = LocalConfiguration.current
                viewModel.windowSize = DpSize(config.screenWidthDp.dp, config.screenHeightDp.dp)
                Column(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                    if (!viewModel.fullScreen) TopAppBar {
                        CommonAppBar(viewModel)
                        if (viewModel.inTabletMode) {
                            IconButton({ finish() }) {
                                Icon(Icons.Default.Close, "Закрыть")
                            }
                        }
                    }
                    App(viewModel)
                }
            }
        }
    }
}