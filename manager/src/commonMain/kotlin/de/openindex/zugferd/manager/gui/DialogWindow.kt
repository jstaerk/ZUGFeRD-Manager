package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState

@Composable
expect fun DialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    resizable: Boolean = true,
    content: @Composable () -> Unit,
)

@Composable
fun LockingDialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    content: @Composable () -> Unit,
) {
    val appState = LocalAppState.current
    appState.setLocked(true)

    DialogWindow(
        title = title,
        onCloseRequest = {
            onCloseRequest()
            appState.setLocked(false)
        },
        width = width,
        height = height,
        content = content,
    )
}
