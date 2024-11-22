package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import java.io.StringWriter


actual fun getPrettyPrintedXml(xml: String): String {
    try {
        /*
        val src = InputSource(StringReader(xml))
        val document: Document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(src)

        val transformer = TransformerFactory
            .newInstance()
            .newTransformer()

        //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        //transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");

        return StringWriter().use { writer ->
            transformer.transform(DOMSource(document), StreamResult(writer))
            writer.toString()
        }
        */

        val format = OutputFormat.createPrettyPrint()
        format.setIndentSize(2)
        format.isSuppressDeclaration = true
        format.encoding = "UTF-8"

        val document = DocumentHelper.parseText(xml)

        return StringWriter().use { writer ->
            XMLWriter(writer, format).write(document)
            writer.toString()
        }
    } catch (e: Exception) {
        APP_LOGGER.error("Error occurs when pretty-printing xml.", e)
        return xml
    }
}
