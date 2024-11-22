package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import io.github.vinceglb.filekit.core.PlatformFile
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.xmpbox.xml.DomXmpParser
import org.apache.xmpbox.xml.XmpParsingException
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import java.io.File

actual suspend fun isPdfArchive(pdfFile: PlatformFile): Boolean {
    return try {
        getPDFAVersion(pdfFile.file) > 0
    } catch (e: Exception) {
        APP_LOGGER.error("Can't check PDF/A version!", e)
        false
    }
}

/**
 * taken from ZUGFeRDExporterFromPDFA
 * @see ZUGFeRDExporterFromPDFA.getPDFAVersion
 */
private fun getPDFAVersion(pdfFile: File): Int {
    val document = Loader.loadPDF(pdfFile)
    val catalog: PDDocumentCatalog = document.getDocumentCatalog()
    val metadata = catalog.metadata

    // the PDF version we could get through the document but we want the PDF-A version,
    // which is different (and can probably base on different PDF versions)
    if (metadata != null) {
        try {
            val xmpParser = DomXmpParser()
            val xmp = xmpParser.parse(metadata.createInputStream())

            val pdfASchema = xmp.pdfaIdentificationSchema
            if (pdfASchema != null) {
                return pdfASchema.part
            }
        } catch (e: XmpParsingException) {
            APP_LOGGER.error("XmpParsingException", e)
        } finally {
            document.close()
        }
    }
    return 0
}
