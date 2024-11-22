package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private val DATE_FORMAT = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber(padding = Padding.SPACE)
    char('.')
    year()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DateField(
    label: String,
    value: LocalDate?,
    clearable: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateState = rememberDatePickerState()
    var showDialog by remember { mutableStateOf(false) }

    TextField(
        label = {
            Text(
                text = label,
                softWrap = false,
            )
        },
        supportingText = supportingText,
        value = value?.format(DATE_FORMAT) ?: "",
        onValueChange = { },
        trailingIcon = {
            Row(
                //horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier,
            ) {
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Default, true),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Datum wählen",
                    )
                }

                if (clearable && value != null) {
                    IconButton(
                        onClick = { onValueChange(null) },
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Default, true),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Datum leeren",
                        )
                    }
                }
            }
        },
        readOnly = true,
        singleLine = true,
        modifier = modifier,
    )

    if (showDialog) {
        DateDialog(
            value = value,
            onValueChange = {
                onValueChange(it)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            },
        )
    }
}