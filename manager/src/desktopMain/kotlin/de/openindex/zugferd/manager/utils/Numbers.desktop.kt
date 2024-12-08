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

package de.openindex.zugferd.manager.utils

import java.text.NumberFormat
import java.util.Locale

private val PERCENTAGE_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 1
            minimumFractionDigits = 1
        }
}

private val PRICE_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
}

private val QUANTITY_FORMAT by lazy {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = false
            maximumFractionDigits = 2
            minimumFractionDigits = 1
        }
}

actual val Number.formatAsPercentage: String
    get() = PERCENTAGE_FORMAT.format(this)

actual val Number.formatAsPrice: String
    get() = PRICE_FORMAT.format(this)

actual val Number.formatAsQuantity: String
    get() = QUANTITY_FORMAT.format(this)
