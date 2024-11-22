package de.openindex.zugferd.manager.utils

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.Serializable

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

enum class ValidationSeverity {
    NOTICE,
    WARNING,
    ERROR,
    FATAL,
}

enum class ValidationType {
    PDF,
    XML,
    OTHER,
}

expect fun getXmlFromPdf(pdf: PlatformFile): String?

expect suspend fun getHtmlVisualizationFromPdf(pdf: PlatformFile): String?

expect suspend fun validatePdf(pdf: PlatformFile): Validation
