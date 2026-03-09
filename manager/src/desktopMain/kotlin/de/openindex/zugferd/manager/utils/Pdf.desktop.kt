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
    //System.err.println("DEBUG: postProcessHtmlForAttachments called")
    if (html.isBlank()) return html
    APP_LOGGER.info("Post-processing HTML for attachments. Found ${attachments.size} attachments in PDF.")
    
    // 1. Save attachments to temp files
    val tempDir = java.nio.file.Files.createTempDirectory("zugferd_attachments_")
    val attachmentFiles = mutableMapOf<String, java.io.File>()
    val attachmentContentMap = mutableMapOf<String, java.io.File>() // Base64 -> File
    
    for ((name, bytes) in attachments) {
        try {
            // Sanitize filename to be safe
            val safeName = name.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val file = tempDir.resolve(safeName).toFile()
            file.writeBytes(bytes)
            file.deleteOnExit() // Clean up when JVM exits
            attachmentFiles[name] = file
            
            // Calculate Base64 for content matching
            val b64 = java.util.Base64.getEncoder().encodeToString(bytes)
            attachmentContentMap[b64] = file
            
            APP_LOGGER.info("Saved attachment '$name' to temporary file: ${file.absolutePath}")
        } catch (e: Exception) {
            APP_LOGGER.error("Failed to save attachment '$name' to temp file", e)
        }
    }

    val doc = Jsoup.parse(html)
    
    // 2. Find all hidden data divs that contain attachment metadata
    // They look like: <div id="..." filename="..." ...>
    // We select divs that have filename attribute (even if empty) or appear to be data containers
    val dataDivs = doc.select("div[filename], div[data-filename], div[mimetype]")
    APP_LOGGER.info("Found ${dataDivs.size} data divs.")

    for ((index, div) in dataDivs.withIndex()) {
        val id = div.id()
        var filename = div.attr("filename").ifEmpty { div.attr("data-filename") }
        
        if (id.isEmpty()) continue
        
        APP_LOGGER.info("Processing data div with ID: $id, Filename: '$filename'")
        
        // Find the corresponding link that calls downloadData('ID')
        val link = doc.select("a[onclick*='$id']").first()
        
        if (link == null) {
            APP_LOGGER.info("No link found for data div ID: $id")
            continue
        }

        // If filename is empty, try to resolve via BT-124 (#ef=filename) in the same block.
        // The data div is inside a boxzeile; its grandparent is the boxtabelle holding all BT-122..BT-125 rows.
        if (filename.isEmpty()) {
            val efHref = div.parent()?.parent()?.selectFirst("a[href^='#ef=']")?.attr("href") ?: ""
            if (efHref.startsWith("#ef=")) {
                val efFilename = efHref.removePrefix("#ef=")
                // Match the key that ends with the #ef= filename (e.g. "EN16931_Elektron_Aufmass.png" ends with "Aufmass.png")
                filename = attachmentFiles.keys.find { it.endsWith(efFilename, ignoreCase = true) } ?: efFilename
                APP_LOGGER.info("Resolved filename via #ef= reference: '$efFilename' → '$filename'")
            }
        }

        // Strategy 1: Try to match by Filename
        var attachmentFile = attachmentFiles[filename]

        if (attachmentFile == null && filename.isNotEmpty()) {
            attachmentFile = attachmentFiles.entries.find { it.key.equals(filename, ignoreCase = true) }?.value
        }

        // Strategy 1b: Try endsWith match (e.g. "Aufmass.png" matches key "EN16931_Elektron_Aufmass.png")
        if (attachmentFile == null && filename.isNotEmpty()) {
            attachmentFile = attachmentFiles.entries.find { it.key.endsWith(filename, ignoreCase = true) }?.value
        }
        
        // Strategy 2: Match by Index (Priority if filename is empty)
        // Since we know we have the files in attachmentFiles and the list of attachments preserves order
        if (attachmentFile == null && index < attachments.size) {
            val (name, _) = attachments[index]
            // We use the name from the PDF attachment list to look up the file
            attachmentFile = attachmentFiles[name]
            
            if (attachmentFile != null) {
                APP_LOGGER.info("Matched attachment by Index ($index) because HTML filename was empty/mismatch. Name: $name")
                if (filename.isEmpty()) filename = name
            }
        }
        
        // Strategy 3: Try to match by Content (Base64) - Fallback
        if (attachmentFile == null) {
            val divContent = div.text().replace("\\s".toRegex(), "")
            if (divContent.isNotEmpty()) {
                attachmentFile = attachmentContentMap[divContent]
                if (attachmentFile != null) {
                    APP_LOGGER.info("Matched attachment by content (Base64) for div ID: $id")
                    if (filename.isEmpty()) filename = attachmentFile.name
                }
            }
        }

        // Strategy 4: Fallback to single attachment
        if (attachmentFile == null && attachmentFiles.size == 1) {
            attachmentFile = attachmentFiles.values.first()
            APP_LOGGER.info("Fallback: Using single attachment for '$filename'")
            if (filename.isEmpty()) filename = attachmentFile.name
        }

        if (attachmentFile != null) {
            // Use data: URL so CEF's onBeforeDownload fires with the correct suggestedName.
            // file:// URLs are rejected by Chrome's download manager before any handler is called.
            val bytes = attachmentFile.readBytes()
            val b64 = java.util.Base64.getEncoder().encodeToString(bytes)
            val resolvedMime = div.attr("mimetype").ifEmpty { div.attr("type") }.ifEmpty { "application/octet-stream" }
            link.attr("href", "data:$resolvedMime;base64,$b64")
            link.attr("download", filename)
            link.removeAttr("onclick")
            link.text("Download")
            APP_LOGGER.info("Replaced link for '$filename' with data URL (${bytes.size} bytes)")
        } else {
            APP_LOGGER.warn("No matching attachment found for filename '$filename' or content")
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