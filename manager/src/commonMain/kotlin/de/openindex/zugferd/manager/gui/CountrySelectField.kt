package de.openindex.zugferd.manager.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.utils.getCountries

@Composable
fun CountrySelectField(
    label: String = "Land",
    country: String? = null,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countries = remember { getCountries() }

    AutoCompleteField(
        label = label,
        entry = country,
        entries = countries,
        onSelect = onSelect,
        modifier = modifier,
    )
}
