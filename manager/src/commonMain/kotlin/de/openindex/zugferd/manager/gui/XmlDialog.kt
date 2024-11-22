package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState

@Composable
fun XmlDialog(
    title: String,
    xml: String,
    onDismissRequest: () -> Unit,
) {
    val appState = LocalAppState.current
    appState.setLocked(true)

    DialogWindow(
        onCloseRequest = {
            onDismissRequest()
            appState.setLocked(false)
        },
        title = title,
        width = 700.dp,
        height = 600.dp,
    ) {
        XmlDialogContent(
            //title = title,
            title = null,
            xml = xml,
            onDismissRequest = {
                onDismissRequest()
                appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        XmlDialogContent(
            title = title,
            xml = xml,
            onDismissRequest = onDismissRequest,
        )
    }
    */
}

@Composable
@Suppress("SameParameterValue")
private fun XmlDialogContent(
    title: String?,
    xml: String,
    onDismissRequest: () -> Unit,
) {
    Box(
        modifier = Modifier
            .border(border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primaryContainer))
            .shadow(elevation = 12.dp)
            //.fillMaxWidth(),
            .fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (title != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            softWrap = false,
                        )
                    }
                }

                TextField(
                    value = xml.trim(),
                    readOnly = true,
                    onValueChange = {},
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                //Spacer(Modifier.weight(1f, true))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f, true),
                    )

                    Button(
                        onClick = { onDismissRequest() },
                    ) {
                        Text(
                            text = "Schließen",
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}
