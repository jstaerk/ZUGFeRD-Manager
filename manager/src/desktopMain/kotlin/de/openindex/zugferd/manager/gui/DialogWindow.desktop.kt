package de.openindex.zugferd.manager.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.DialogWindow as DesktopDialogWindow

@Composable
actual fun DialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp,
    height: Dp,
    resizable: Boolean,
    content: @Composable () -> Unit,
) {
    val dialogWindowState = rememberDialogState(
        position = WindowPosition.PlatformDefault,
        width = width,
        height = height,
    )

    DesktopDialogWindow(
        onCloseRequest = onCloseRequest,
        state = dialogWindowState,
        title = title,
        resizable = resizable,
    ) {
        content()
    }
}
