package de.openindex.zugferd.manager.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.utils.getCurrencies

@Composable
fun CurrencySelectField(
    label: String = "Währung",
    currency: String? = null,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencies = remember { getCurrencies() }

    AutoCompleteField(
        label = label,
        entry = currency,
        entries = currencies,
        onSelect = onSelect,
        modifier = modifier,
    )
}
