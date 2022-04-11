import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import me.demo.common.App
import me.demo.common.CommonAppBar
import me.demo.common.MainViewModel

@Composable
fun WindowScope.DraggableTopAppBar(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) = if (enabled) WindowDraggableArea(modifier) {
    TopAppBar(content = content)
} else Box(modifier) {
    TopAppBar(content = content)
}

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
    MaterialTheme(
        colors = if (darkTheme) darkColors(primaryVariant = Color.Black) else lightColors(),
        content = content
    )

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    val viewModel = MainViewModel()
    viewModel.getViewParams()
    val state = rememberWindowState()
    Window(::exitApplication, state,
        title = "Расписание", undecorated = true, icon = painterResource("ic_launcher_lrmk.png")) {
        AppTheme(viewModel.darkMode) {
            Column(Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .border(1.dp, MaterialTheme.colors.primaryVariant)
            ) {
                if (!viewModel.fullScreen) DraggableTopAppBar(
                    state.placement == WindowPlacement.Floating,
                    Modifier.height(30.dp).combinedClickable(onDoubleClick = {
                        if (state.placement == WindowPlacement.Maximized)
                            state.placement = WindowPlacement.Floating
                        else
                            state.placement = WindowPlacement.Maximized
                    }) {}
                ) {
                    viewModel.windowSize = state.size
                    CommonAppBar(viewModel)

                    IconButton({ state.isMinimized = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Свернуть")
                    }
                    if (state.placement == WindowPlacement.Maximized)
                        IconButton({ state.placement = WindowPlacement.Floating }) {
                            Icon(Icons.Default.KeyboardArrowDown, "Восстановить")
                        }
                    else
                        IconButton({ state.placement = WindowPlacement.Maximized }) {
                            Icon(Icons.Default.KeyboardArrowUp, "Развернуть")
                        }
                    IconButton(::exitApplication) {
                        Icon(Icons.Default.Close, "Закрыть")
                    }
                }
                App(viewModel)
            }
        }
    }
}