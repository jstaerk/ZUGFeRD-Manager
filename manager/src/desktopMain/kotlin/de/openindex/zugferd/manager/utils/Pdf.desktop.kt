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

actual fun getAttachmentsFromPdf(pdf: PlatformFile): List<Pair<String, ByteArray>> {
    return try {
        val doc = Loader.loadPDF(pdf.file)
        val result = mutableListOf<Pair<String, ByteArray>>()
        try {
            val nameTree = PDDocumentNameDictionary(doc.documentCatalog).embeddedFiles ?: return emptyList()
            for ((name, spec) in (nameTree.names ?: return emptyList())) {
                val ef = (spec as? PDComplexFileSpecification)?.let {
                    it.embeddedFile ?: it.embeddedFileUnicode
                } ?: continue
                result += name to ef.createInputStream().readBytes()
            }
        } finally {
            doc.close()
        }
        result
    } catch (e: Exception) {
        APP_LOGGER.error("Anhänge konnten nicht aus PDF gelesen werden.", e)
        emptyList()
    }
}

actual fun postProcessHtmlForAttachments(html: String, attachments: List<Pair<String, ByteArray>>): String {
    if (html.isBlank()) return html
    APP_LOGGER.info("Post-processing HTML for attachments. Found ${attachments.size} attachments in PDF.")

    // Keep bytes in memory — no temp files needed.
    val attachmentData = mutableMapOf<String, ByteArray>()        // original name → bytes
    val attachmentContentMap = mutableMapOf<String, String>()     // base64 content → original name

    for ((name, bytes) in attachments) {
        attachmentData[name] = bytes
        attachmentContentMap[java.util.Base64.getEncoder().encodeToString(bytes)] = name
        APP_LOGGER.info("Registered attachment '$name' (${bytes.size} bytes)")
    }

    val doc = Jsoup.parse(html)
    val dataDivs = doc.select("div[filename], div[data-filename], div[mimetype]")
    APP_LOGGER.info("Found ${dataDivs.size} data divs.")

    for ((index, div) in dataDivs.withIndex()) {
        val id = div.id()
        if (id.isEmpty()) continue

        var filename = div.attr("filename").ifEmpty { div.attr("data-filename") }
        APP_LOGGER.info("Processing data div with ID: $id, Filename: '$filename'")

        val link = doc.select("a[onclick*='$id']").first() ?: run {
            APP_LOGGER.info("No link found for data div ID: $id")
            return@run null
        } ?: continue

        // Resolve filename from BT-124 #ef= reference (grandparent = boxtabelle container).
        if (filename.isEmpty()) {
            val efHref = div.parent()?.parent()?.selectFirst("a[href^='#ef=']")?.attr("href") ?: ""
            if (efHref.startsWith("#ef=")) {
                val efFilename = efHref.removePrefix("#ef=")
                filename = attachmentData.keys.find { it.endsWith(efFilename, ignoreCase = true) } ?: efFilename
                APP_LOGGER.info("Resolved filename via #ef= reference: '$efFilename' → '$filename'")
            }
        }

        // Strategy 1: exact match
        var resolvedName: String? = attachmentData.keys.find { it == filename }
        // Strategy 1b: case-insensitive
        if (resolvedName == null && filename.isNotEmpty())
            resolvedName = attachmentData.keys.find { it.equals(filename, ignoreCase = true) }
        // Strategy 1c: endsWith (e.g. "Aufmass.png" matches "EN16931_Elektron_Aufmass.png")
        if (resolvedName == null && filename.isNotEmpty())
            resolvedName = attachmentData.keys.find { it.endsWith(filename, ignoreCase = true) }
        // Strategy 2: index-based
        if (resolvedName == null && index < attachments.size) {
            resolvedName = attachments[index].first
            APP_LOGGER.info("Matched attachment by index ($index): $resolvedName")
            if (filename.isEmpty()) filename = resolvedName
        }
        // Strategy 3: base64 content match
        if (resolvedName == null) {
            val divContent = div.text().replace("\\s".toRegex(), "")
            if (divContent.isNotEmpty()) {
                resolvedName = attachmentContentMap[divContent]
                if (resolvedName != null) {
                    APP_LOGGER.info("Matched attachment by content for div ID: $id")
                    if (filename.isEmpty()) filename = resolvedName
                }
            }
        }
        // Strategy 4: single attachment fallback
        if (resolvedName == null && attachmentData.size == 1) {
            resolvedName = attachmentData.keys.first()
            APP_LOGGER.info("Fallback: single attachment used for '$filename'")
            if (filename.isEmpty()) filename = resolvedName
        }

        if (resolvedName != null) {
            val bytes = attachmentData[resolvedName]!!
            val mimeType = div.attr("mimetype").ifEmpty { div.attr("type") }.ifEmpty { "application/octet-stream" }

            // Register bytes in global map — served on demand by the in-memory HTTP server.
            globalAttachmentData[filename] = Pair(bytes, mimeType)

            val encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8")
            link.attr("href", "zugferd-attachment://$encodedFilename")
            link.removeAttr("onclick")
            link.removeAttr("download")
            link.text("Anhang öffnen")
            APP_LOGGER.info("Replaced link for '$filename' with in-memory attachment reference (${bytes.size} bytes)")
        } else {
            APP_LOGGER.warn("No matching attachment found for filename '$filename'")
        }
    }

    return doc.outerHtml()
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

actual suspend fun getHtmlVisualizationFromXML(xml: PlatformFile): String? {
    return getHtmlVisualizationFromXML(xml.file.toPath())
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

    val html = getHtmlVisualizationFromXML(tempXmlFile) ?: return null
    val attachments = getAttachmentsFromPdf(pdf)
    return postProcessHtmlForAttachments(html, attachments)
}