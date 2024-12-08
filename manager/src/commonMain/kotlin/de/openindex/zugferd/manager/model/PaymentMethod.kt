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

package de.openindex.zugferd.manager.model

enum class PaymentMethod(
    val code: Int,
    val description: String
) {
    /**
     * In Cash.
     */
    //CASH(10, "Barzahlung"),

    /**
     * Credit Transfer.
     */
    //CREDIT_TRANSFER(30, "Überweisung"),

    /**
     * Debit Transfer.
     */
    //DEBIT_TRANSFER(31, "Bankeinzug"),

    /**
     * Payment to bank account.
     */
    //PAYMENT_TO_BANK_ACCOUNT(42, "Einzahlung auf Konto"),

    /**
     * Bank card.
     */
    //BANK_CARD(48, "Bank-Karte"),

    /**
     * Direct Debit.
     */
    //DIRECT_DEBIT(49, "Abbuchung"),

    /**
     * Standing agreement.
     */
    //STANDING_AGREEMENT(57, "Abkommen"),

    /**
     * SEPA credit transfer.
     */
    SEPA_CREDIT_TRANSFER(58, "SEPA-Überweisung"),

    /**
     * SEPA direct debit.
     */
    SEPA_DIRECT_DEBIT(59, "SEPA-Lastschrift"),

    /**
     * Clearing between partners
     */
    //CLEARING_BETWEEN_PARTNERS(97, "partnerschaftliche Verrechnung"),

    ;
}
