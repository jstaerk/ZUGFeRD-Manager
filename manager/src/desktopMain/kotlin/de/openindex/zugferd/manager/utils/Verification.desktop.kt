package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import org.mustangproject.validator.EPart
import org.mustangproject.validator.ESeverity
import org.mustangproject.validator.ValidationContext
import org.mustangproject.validator.ValidationResultItem
import org.mustangproject.validator.ZUGFeRDValidator
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.pathString
import kotlin.io.path.writer


@Suppress("SpellCheckingInspection")
private val CUSTOM_VISUALIZATION_CSS = """
    body > form {
        position: fixed !important;
        left: 0 !important;
        right: 0 !important;
        z-index: 1 !important;
    }

    .inhalt {
        padding-top: 75px !important;    
    }
    
    .menue > .innen {
        text-align: center !important;
    }
    
    .menue > .innen > button {
        font-family: sans-serif !important;
        font-size: 14px !important;
    }
""".trimIndent()

private class CustomValidator : ZUGFeRDValidator() {
    val cachedResultItems: MutableList<ValidationResultItem> = mutableListOf()
    var cachedSignature: String? = null

    init {
        context = object : ValidationContext(APP_LOGGER) {
            override fun addResultItem(vr: ValidationResultItem?) {
                super.addResultItem(vr)
                if (vr != null) {
                    cachedResultItems.add(vr)
                }
            }

            override fun setSignature(signature: String?): ValidationContext {
                if (cachedSignature == null && signature != null) {
                    cachedSignature = signature
                }
                return super.setSignature(signature)
            }
        }
    }

    fun getValidation(inputStream: InputStream, fileNameOfInputStream: String): Validation {
        validate(inputStream, fileNameOfInputStream)

        return Validation(
            isValid = wasCompletelyValid,
            signature = cachedSignature,
            profile = context.profile,
            version = context.generation,
            messages = cachedResultItems.map { getMessage(it) },
        )
    }

    private fun getMessage(item: ValidationResultItem): ValidationMessage {
        return ValidationMessage(
            message = item.message,
            severity = when (item.severity) {
                ESeverity.notice -> ValidationSeverity.NOTICE
                ESeverity.warning -> ValidationSeverity.WARNING
                ESeverity.error -> ValidationSeverity.ERROR
                else -> ValidationSeverity.FATAL
            },
            type = when (item.part) {
                EPart.pdf -> ValidationType.PDF
                EPart.fx, EPart.ox -> ValidationType.XML
                else -> ValidationType.OTHER
            },
        )
    }
}

actual fun getXmlFromPdf(pdf: PlatformFile): String? {
    return try {
        pdf.file
            .inputStream().use { input ->
                ZUGFeRDImporter(input)
            }
            .utF8
            ?.trimToNull()
    } catch (e: Exception) {
        APP_LOGGER.error("PDF read error.", e)
        null
    }
}

actual suspend fun validatePdf(pdf: PlatformFile): Validation {
    return pdf.file
        .inputStream().use { input ->
            CustomValidator()
                .getValidation(input, pdf.name)
        }
}

actual suspend fun getHtmlVisualizationFromPdf(pdf: PlatformFile): String? {
    val xmlData = getXmlFromPdf(pdf) ?: return null
    val tempXmlFile = withContext(Dispatchers.IO) {
        val tempXmlFile = Files.createTempFile("zugferd-", ".xml")
        tempXmlFile.writer().use { writer ->
            writer.write(xmlData)
        }
        tempXmlFile
    }

    return try {
        ZUGFeRDVisualizer()
            .visualize(
                tempXmlFile.pathString,
                ZUGFeRDVisualizer.Language.DE,
            )
            // HACK: Apply custom css.
            .replace(
                "</head>",
                "\n<style>\n${CUSTOM_VISUALIZATION_CSS}</style>\n</head>"
            )

        //APP_LOGGER.debug("generated HTML\n${html}")
    } catch (e: Exception) {
        APP_LOGGER.error("Can't create HTML visualization.", e)
        null
    }

    /*
    try {
        ExportResource("/xrechnung-viewer.css")
        ExportResource("/xrechnung-viewer.js")

        println("xrechnung-viewer.css and xrechnung-viewer.js written as well (to local working dir)")
    } catch (e: java.lang.Exception) {
        LOGGER.error(e.message, e)
    }
    */
}