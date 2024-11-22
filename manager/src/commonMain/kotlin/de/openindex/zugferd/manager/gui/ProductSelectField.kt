package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.Product

@Composable
fun ProductSelectField(
    label: String = "Rechnungsposten",
    product: Product? = null,
    products: List<Product>,
    onSelect: (Product) -> Unit,
    //actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val options = remember {
        buildMap {
            products.forEach { party ->
                put(party, party.summary)
            }
        }
    }

    AutoCompleteField(
        label = label,
        entry = product,
        entries = options,
        onSelect = onSelect,
        //actions = actions,
        modifier = modifier,
    )
}

@Composable
fun ProductSelectFieldWithAdd(
    label: String = "Rechnungsposten",
    addLabel: String = "Neuer Rechnungsposten",
    product: Product? = null,
    products: List<Product>,
    onSelect: (product: Product, savePermanently: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newProduct by remember { mutableStateOf<Product?>(null) }

    val options by remember {
        mutableStateOf(
            products.toMutableList()
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ProductSelectField(
            label = label,
            product = product,
            products = options,
            onSelect = { selection ->
                onSelect(selection, false)
            },
            modifier = Modifier.weight(1f, fill = true),
        )

        Tooltip(
            text = addLabel,
        ) {
            IconButton(
                onClick = {
                    newProduct = Product()
                },
                modifier = Modifier,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hinzufügen",
                )
            }
        }
    }

    /*
    ProductSelectField(
        label = label,
        product = product,
        products = options,
        onSelect = { selection ->
            onSelect(selection, false)
        },
        actions = {
            Tooltip(
                text = addLabel,
            ) {
                IconButton(

                    onClick = {
                        newProduct = Product()
                    },
                    modifier = Modifier,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Hinzufügen",
                    )
                }
            }
        },
        modifier = modifier,
    )
    */

    if (newProduct != null) {
        ProductDialog(
            title = addLabel,
            value = newProduct!!,
            permanentSaveOption = true,
            onDismissRequest = { newProduct = null },
            onSubmitRequest = { selection, savePermanently ->
                options.addFirst(selection)
                onSelect(selection, savePermanently)
                newProduct = null
            },
        )
    }
}
