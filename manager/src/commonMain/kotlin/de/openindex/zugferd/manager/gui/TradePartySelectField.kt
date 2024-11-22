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
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.utils.getDefaultCountryCode

@Composable
fun TradePartySelectField(
    label: String = "Partner",
    tradeParty: TradeParty? = null,
    tradeParties: List<TradeParty>,
    onSelect: (TradeParty) -> Unit,
    //actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val options = remember {
        buildMap {
            tradeParties.forEach { party ->
                put(party, party.summary)
            }
        }
    }

    AutoCompleteField(
        label = label,
        entry = tradeParty,
        entries = options,
        onSelect = onSelect,
        //actions = actions,
        modifier = modifier,
    )
}

@Composable
fun TradePartySelectFieldWithAdd(
    label: String = "Partner",
    addLabel: String = "Neuer Partner",
    tradeParty: TradeParty? = null,
    tradeParties: List<TradeParty>,
    onSelect: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newTradeParty by remember { mutableStateOf<TradeParty?>(null) }

    val options by remember {
        mutableStateOf(
            tradeParties.toMutableList()
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        TradePartySelectField(
            label = label,
            tradeParty = tradeParty,
            tradeParties = options,
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
                    newTradeParty = TradeParty(
                        country = getDefaultCountryCode(),
                    )
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
    TradePartySelectField(
        label = label,
        tradeParty = tradeParty,
        tradeParties = options,
        onSelect = { selection ->
            onSelect(selection, false)
        },
        actions = {
            Tooltip(
                text = addLabel,
            ) {
                IconButton(
                    onClick = {
                        newTradeParty = TradeParty()
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

    if (newTradeParty != null) {
        TradePartyDialog(
            title = addLabel,
            value = newTradeParty!!,
            permanentSaveOption = true,
            onDismissRequest = { newTradeParty = null },
            onSubmitRequest = { selection, savePermanently ->
                options.addFirst(selection)
                onSelect(selection, savePermanently)
                newTradeParty = null
            },
        )
    }
}
