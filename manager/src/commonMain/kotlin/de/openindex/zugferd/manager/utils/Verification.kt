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

import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationSeverity_ERROR
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationSeverity_FATAL
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationSeverity_NOTICE
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationSeverity_WARNING
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationType_OTHER
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationType_PDF
import de.openindex.zugferd.zugferd_manager.generated.resources.ValidationType_XML
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
data class Validation(
    val isValid: Boolean,
    val signature: String?,
    val profile: String?,
    val version: String?,
    val messages: List<ValidationMessage>
) {
    val countNotices: Int
        get() = messages.count { it.severity == ValidationSeverity.NOTICE }

    val countWarnings: Int
        get() = messages.count { it.severity == ValidationSeverity.WARNING }

    val countErrors: Int
        get() = messages.count { it.severity == ValidationSeverity.ERROR || it.severity == ValidationSeverity.FATAL }
}

@Serializable
data class ValidationMessage(
    val message: String,
    val type: ValidationType,
    val severity: ValidationSeverity,
)

enum class ValidationSeverity(val title: StringResource) {
    NOTICE(
        title = Res.string.ValidationSeverity_NOTICE,
    ),
    WARNING(
        title = Res.string.ValidationSeverity_WARNING,
    ),
    ERROR(
        title = Res.string.ValidationSeverity_ERROR,
    ),
    FATAL(
        title = Res.string.ValidationSeverity_FATAL,
    ),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)
}

enum class ValidationType(val title: StringResource) {
    PDF(
        title = Res.string.ValidationType_PDF,
    ),
    XML(
        title = Res.string.ValidationType_XML,
    ),
    OTHER(
        title = Res.string.ValidationType_OTHER,
    ),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)
}

expect fun getXmlFromPdf(pdf: PlatformFile): String?

expect suspend fun getHtmlVisualizationFromPdf(pdf: PlatformFile): String?

expect suspend fun validatePdf(pdf: PlatformFile): Validation
