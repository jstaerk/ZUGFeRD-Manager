/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.model.BankDetails
import de.openindex.zugferd.manager.model.Contact
import de.openindex.zugferd.manager.model.TradeParty

private enum class TradePartyForm {
    GENERAL,
    CONTACT,
    BANK_ACCOUNT,
    DESCRIPTION;

    fun title(): String = when (this) {
        GENERAL -> "Allgemein"
        CONTACT -> "Kontakt"
        BANK_ACCOUNT -> "Konto"
        DESCRIPTION -> "Freitexte"
    }
}

@Composable
fun TradePartyDialog(
    title: String,
    value: TradeParty,
    isCustomer: Boolean = false,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
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
        TradePartyDialogContent(
            //title = title,
            title = null,
            value = value,
            isCustomer = isCustomer,
            permanentSaveOption = permanentSaveOption,
            onDismissRequest = {
                onDismissRequest()
                appState.setLocked(false)
            },
            onSubmitRequest = { tradeParty, savePermanently ->
                onSubmitRequest(tradeParty, savePermanently)
                appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        TradePartyDialogContent(
            title = title,
            value = value,
            onDismissRequest = onDismissRequest,
            onSubmitRequest = onSubmitRequest,
        )
    }
    */
}

@Composable
@Suppress("SameParameterValue")
private fun TradePartyDialogContent(
    title: String?,
    value: TradeParty,
    isCustomer: Boolean = false,
    permanentSaveOption: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
) {
    var tradeParty by remember { mutableStateOf(value.copy()) }
    var savePermanently by remember { mutableStateOf(false) }

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

                TradePartyForm(
                    value = tradeParty,
                    isCustomer = isCustomer,
                    onUpdate = { tradeParty = it },
                )

                Spacer(Modifier.weight(1f, true))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Button(
                        onClick = {
                            onSubmitRequest(
                                tradeParty,
                                savePermanently,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    ) {
                        Text(
                            text = "Übernehmen",
                            softWrap = false,
                        )
                    }

                    if (permanentSaveOption) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clickable { savePermanently = !savePermanently },
                        ) {
                            Switch(
                                checked = savePermanently,
                                onCheckedChange = { savePermanently = it },
                            )
                            Text(
                                text = "dauerhaft speichern",
                                softWrap = false,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .weight(1f, true),
                    )

                    Button(
                        onClick = { onDismissRequest() },
                    ) {
                        Text(
                            text = "Abbrechen",
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TradePartyForm(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    var state by remember { mutableStateOf(0) }

    TabRow(
        selectedTabIndex = state,
    ) {
        TradePartyForm.entries.forEachIndexed { index, form ->
            Tab(
                selected = state == index,
                onClick = { state = index },
                text = {
                    Text(
                        text = form.title(),
                        softWrap = false,
                    )
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        AnimatedVisibility(visible = state == 0) {
            TradePartyFormGeneral(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }
        AnimatedVisibility(visible = state == 1) {
            TradePartyFormContact(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }
        AnimatedVisibility(visible = state == 2) {
            TradePartyFormBankAccount(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }
        AnimatedVisibility(visible = state == 3) {
            TradePartyFormDescription(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }
    }
}

@Composable
private fun TradePartyFormGeneral(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        TextField(
            value = value.name,
            label = {
                Text(
                    text = "Name",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(name = it)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            value = value.street ?: "",
            label = {
                Text(
                    text = "Straße",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(street = it)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            value = value.additionalAddress ?: "",
            label = {
                Text(
                    text = "Adresszusatz",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(additionalAddress = it)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TextField(
                value = value.zip ?: "",
                label = {
                    Text(
                        text = "PLZ",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(zip = it)
                    )
                },
                modifier = Modifier
                    .weight(0.3f),
            )

            TextField(
                value = value.location ?: "",
                label = {
                    Text(
                        text = "Ort",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(location = it)
                    )
                },
                modifier = Modifier
                    .weight(0.7f),
            )
        }

        CountrySelectField(
            country = value.country,
            label = "Land",
            onSelect = {
                onUpdate(
                    value.copy(country = it)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TextField(
                value = value.vatID ?: "",
                label = {
                    Text(
                        text = "USt-Id-Nr",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(vatID = it)
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            TextField(
                value = value.taxID ?: "",
                label = {
                    Text(
                        text = "Steuer-Nr",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(taxID = it)
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            if (isCustomer) {
                TextField(
                    value = value.id ?: "",
                    label = {
                        Text(
                            text = "Kunden-Nr",
                            softWrap = false,
                        )
                    },
                    singleLine = true,
                    onValueChange = {
                        onUpdate(
                            value.copy(id = it)
                        )
                    },
                    modifier = Modifier
                        .weight(0.33f),
                )
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun TradePartyFormContact(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    val contact by remember(value.contact) {
        mutableStateOf(value.contact ?: Contact())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        TextField(
            value = contact.name,
            label = {
                Text(
                    text = "Name",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(contact = contact.copy(name = it))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            value = contact.street ?: "",
            label = {
                Text(
                    text = "Straße",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(contact = contact.copy(street = it))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TextField(
                value = contact.zip ?: "",
                label = {
                    Text(
                        text = "PLZ",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(contact = contact.copy(zip = it))
                    )
                },
                modifier = Modifier
                    .weight(0.3f),
            )

            TextField(
                value = contact.location ?: "",
                label = {
                    Text(
                        text = "Ort",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(contact = contact.copy(location = it))
                    )
                },
                modifier = Modifier
                    .weight(0.7f),
            )
        }

        CountrySelectField(
            country = contact.country,
            label = "Land",
            onSelect = {
                onUpdate(
                    value.copy(contact = contact.copy(country = it))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            TextField(
                value = contact.email ?: "",
                label = {
                    Text(
                        text = "E-Mail",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(contact = contact.copy(email = it))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            TextField(
                value = contact.phone ?: "",
                label = {
                    Text(
                        text = "Telefon",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(contact = contact.copy(phone = it))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            TextField(
                value = contact.fax ?: "",
                label = {
                    Text(
                        text = "Fax",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(contact = contact.copy(fax = it))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )
        }
    }
}

@Composable
private fun TradePartyFormBankAccount(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    val account by remember(value.bankDetails) {
        mutableStateOf(value.bankDetails.firstOrNull() ?: BankDetails())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        TextField(
            value = account.accountName ?: "",
            label = {
                Text(
                    text = "Kontoinhaber",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(accountName = it)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            value = account.iban,
            label = {
                Text(
                    text = "IBAN",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(iban = it)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            value = account.bic ?: "",
            label = {
                Text(
                    text = "BIC",
                    softWrap = false,
                )
            },
            singleLine = true,
            onValueChange = {
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(bic = it)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        if (!isCustomer) {
            TextField(
                value = value.creditorReferenceId ?: "",
                label = {
                    Text(
                        text = "SEPA-Gläubigerkennung",
                        softWrap = false,
                    )
                },
                singleLine = true,
                onValueChange = {
                    onUpdate(
                        value.copy(creditorReferenceId = it)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }

        if (isCustomer) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                PaymentMethodDropDown(
                    value = value._defaultPaymentMethod,
                    label = "Bevorzugte Zahlungsart",
                    onSelect = {
                        onUpdate(
                            value.copy(_defaultPaymentMethod = it)
                        )
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                TextField(
                    value = account.directDebitMandateId ?: "",
                    label = {
                        Text(
                            text = "SEPA-Mandatsreferenz",
                            softWrap = false,
                        )
                    },
                    singleLine = true,
                    onValueChange = {
                        onUpdate(
                            value.copy(bankDetails = listOf(account.copy(directDebitMandateId = it)))
                        )
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TradePartyFormDescription(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        if (!isCustomer) {
            Tooltip(
                text = "Weitere Impressums-Angaben zum Unternehmen, die in der Regel im Briefkopf einer Rechnung stehen." +
                        "Zum Beispiel: Gesellschafter, Vorstand, Handelsregister, zuständiges Registergericht, etc."
            ) {
                TextField(
                    value = value.imprint ?: "",
                    label = {
                        Text(
                            text = "Impressum",
                            softWrap = false,
                        )
                    },
                    onValueChange = {
                        onUpdate(
                            value.copy(
                                imprint = it
                            )
                        )
                    },
                    singleLine = false,
                    modifier = Modifier
                        .heightIn(min = 150.dp, max = 200.dp)
                        .fillMaxWidth(),
                )
            }
        }

        Tooltip(
            text = "Hier können interne Notizen hinterlegt werden. Diese werden NICHT in E-Rechnungen veröffentlicht."
        ) {
            TextField(
                value = value.description ?: "",
                label = {
                    Text(
                        text = "Interne Notizen",
                        softWrap = false,
                    )
                },
                onValueChange = {
                    onUpdate(
                        value.copy(
                            description = it
                        )
                    )
                },
                singleLine = false,
                modifier = Modifier
                    .heightIn(min = 150.dp, max = if (isCustomer) 300.dp else 200.dp)
                    .fillMaxWidth(),
            )
        }
    }
}
