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

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.APP_TITLE_FULL
import de.openindex.zugferd.manager.APP_VERSION
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode
import org.apache.pdfbox.pdmodel.common.PDMetadata
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent
import org.apache.xmpbox.xml.DomXmpParser
import org.apache.xmpbox.xml.XmpParsingException
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Collections
import java.util.GregorianCalendar
import kotlin.io.path.pathString
import kotlin.io.path.writer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

actual suspend fun getPdfArchiveVersion(pdfFile: PlatformFile): Int {
    return try {
        getPDFAVersion(pdfFile.file)
    } catch (e: Exception) {
        APP_LOGGER.error("Can't extract PDF/A version!", e)
        -1
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

    // the PDF version we could get through the document, but we want the PDF-A version,
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



@Throws(IOException::class)
fun PDDocument.removeEmbeddedFiles(): PDDocument {
    val names = PDDocumentNameDictionary(documentCatalog)
    names.embeddedFiles = PDEmbeddedFilesNameTreeNode()
    documentCatalog.names = names
    return this
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

/*
actual suspend fun getHtmlVisualizationFromXML(xml: Path): String? {
    return try {
        ZUGFeRDVisualizer()
            .visualize(
                xml.pathString,
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
}

 */



/*
actual suspend fun getHtmlVisualizationFromXML(xml: Path): String? {
    return try {
        val rawHtml = ZUGFeRDVisualizer()
            .visualize(xml.pathString, ZUGFeRDVisualizer.Language.DE)

        val doc: Document = Jsoup.parse(rawHtml)

        // Fügt benutzerdefiniertes CSS ein
        val styleTag = doc.head().appendElement("style")
        styleTag.appendText(CUSTOM_VISUALIZATION_CSS)

        // Schleife über alle "boxzeile"-Container
        val boxRows = doc.select("div.boxzeile")
        for (row in boxRows) {
            val legend = row.selectFirst("div.boxdaten.legende")
            val value = row.selectFirst("div.boxdaten.wert[id]")

            if (legend != null && value != null) {
                val btId = value.id()
                // Falls nicht schon vorhanden, anhängen
                if (!legend.text().contains(btId)) {
                    val labelText = legend.text().removeSuffix(":").trim()
                    println(labelText)
                    legend.text("$labelText $btId")
                }
            }
        }


        // Erzeugen HTML, Speichern und im Browser öffnen
        val htmlContent = doc.outerHtml()

        // Datei speichern
        val htmlFile = Files.createTempFile("zugferd-", ".html")
        Files.write(htmlFile, htmlContent.toByteArray(Charsets.UTF_8))
        println("HTML gespeichert unter: ${htmlFile.toAbsolutePath()}")

        // Optional im Browser öffnen
        try {
            java.awt.Desktop.getDesktop().browse(htmlFile.toUri())
        } catch (e: Exception) {
            println(" Browser konnte nicht geöffnet werden: ${e.message}")
        }

        htmlContent
    } catch (e: Exception) {
        APP_LOGGER.error("Can't create HTML visualization.", e)
        null
    }
}

 */

actual suspend fun getHtmlVisualizationFromXML(xml: Path): String? {
    return try {
        val rawHtml = ZUGFeRDVisualizer()
            .visualize(xml.pathString, ZUGFeRDVisualizer.Language.DE)

        val doc: Document = Jsoup.parse(rawHtml)

        //File("zugferd_test.html").writeText(doc.outerHtml())



        // Benutzerdefiniertes CSS einfügen
        doc.head().appendElement("style").appendText(CUSTOM_VISUALIZATION_CSS)

        // 1. BT-IDs in boxzeile
        val boxRows = doc.select("div.boxzeile")
        for (row in boxRows) {
            val legend = row.selectFirst("div.boxdaten.legende")
            val value = row.selectFirst("div.boxdaten.wert[id]")

            if (legend != null && value != null) {
                val btId = value.id()
                if (!legend.text().contains(btId)) {
                    val labelText = legend.text().removeSuffix(":").trim()
                    legend.text("$labelText [$btId]")
                }
            }
        }

        //  2. BT-IDs in rechnungsZeile → rechnungSp1 bekommt die ID von rechnungSp3
        val invoiceRows = doc.select("div.rechnungsZeile")
        for (row in invoiceRows) {
            val label = row.selectFirst("div.rechnungSp1")
            val idSource = row.selectFirst("div.rechnungSp3[id]")

            if (label != null && idSource != null) {
                val btId = idSource.id()
                if (!label.text().contains(btId)) {
                    val text = label.text().removeSuffix(":").trim()
                    label.text("$text [$btId]")
                }
            }
        }


        doc.outerHtml()
    } catch (e: Exception) {
        APP_LOGGER.error("Can't create HTML visualization.", e)
        null
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

    return getHtmlVisualizationFromXML(tempXmlFile)
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