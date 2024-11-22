package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.APP_TITLE_FULL
import de.openindex.zugferd.manager.APP_VERSION
import de.openindex.zugferd.manager.utils.toJavaDate
import io.github.vinceglb.filekit.core.PlatformFile
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.mustangproject.XMLTools
import org.mustangproject.ZUGFeRD.IExportableTransaction
import org.mustangproject.ZUGFeRD.Profiles
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Locale
import org.mustangproject.Invoice as _Invoice


const val INVOICE_PROFILE = "EXTENDED"

private val DATE_FORMAT = DateFormat
    .getDateInstance(DateFormat.MEDIUM, Locale.GERMANY)

fun Invoice.build(method: PaymentMethod): _Invoice {
    return _Invoice()
        .setCurrency(currency)
        .setNumber(number)
        .setIssueDate(issueDate.toJavaDate())
        .setDueDate(dueDate.toJavaDate())
        .setPaymentTermDescription(
            if (method == PaymentMethod.SEPA_DIRECT_DEBIT)
                "Abbuchung erfolgt am ${DATE_FORMAT.format(dueDate.toJavaDate())}."
            else
                "Bitte bis ${DATE_FORMAT.format(dueDate.toJavaDate())} überweisen."
        )
        .setCreditorReferenceID(
            sender?.creditorReferenceId
        )
        .let { invoice ->
            @Suppress("LocalVariableName")
            val _sender = if (method == PaymentMethod.SEPA_DIRECT_DEBIT) {
                /**
                 * Direct Debit requires recipients bank account
                 * including mandate reference within senders debit details.
                 * @see org.mustangproject.Invoice#getTradeSettlement()
                 * */
                sender?.copy(
                    bankDetails = listOf(),
                    debitDetails = buildList {
                        recipient?.bankDetails?.firstOrNull()?.let {
                            add(
                                DirectDebit(
                                    iban = it.iban,
                                    mandate = it.directDebitMandateId ?: "",
                                )
                            )
                        }
                    },
                )
            } else {
                sender
            }

            val party = _sender?.build()
            if (party != null) {
                invoice.setSender(party)
            }

            invoice
        }
        .let { invoice ->
            val party = recipient?.build()
            if (party != null) {
                invoice.setRecipient(party)
            }

            invoice
        }
        .let { invoice ->
            items
                .mapNotNull { it.build() }
                .forEach { invoice.addItem(it) }

            invoice
        }
}

/*actual fun Invoice.isValid(): Boolean {
    return build().isValid
}*/

actual fun Invoice.toXml(method: PaymentMethod): String {
    val transaction = build(
        method = method,
    )

    //val tradeSettlements = transaction.tradeSettlement ?: listOf<IZUGFeRDTradeSettlement>().toTypedArray()
    //APP_LOGGER.debug("TRADE SETTLEMENTS / ${tradeSettlements.size}")
    //if (transaction.sender == null) {
    //    APP_LOGGER.debug("SENDER IS NULL")
    //} else if (transaction.sender.bankDetails.isEmpty() && transaction.sender.debitDetails.isEmpty()) {
    //    APP_LOGGER.debug("SENDER BANKING DETAILS ARE EMPTY")
    //}
    //APP_LOGGER.debug("-".repeat(50))
    //tradeSettlements.forEach {
    //    APP_LOGGER.debug(it.settlementXML)
    //    APP_LOGGER.debug("-".repeat(50))
    //}

    //val zf2p = ZUGFeRD2PullProvider()
    val zf2p = CustomZUGFeRD2PullProvider()
    zf2p.profile = Profiles.getByName(INVOICE_PROFILE)
    zf2p.generateXML(transaction)
    return String(zf2p.xml, StandardCharsets.UTF_8).trim()
}

/**
 * https://www.mustangproject.org/invoice-class/?lang=de
 */
actual suspend fun Invoice.export(
    sourceFile: PlatformFile,
    targetFile: PlatformFile,
    method: PaymentMethod,
): Boolean {
    val exporter = try {
        sourceFile.file.inputStream().use { input ->
            //ZUGFeRDExporterFromPDFA()
            CustomZUGFeRDExporterFromPDFA()
                .load(input)
        }
            .setProducer("$APP_TITLE_FULL $APP_VERSION")
            .setCreator("$APP_TITLE_FULL $APP_VERSION")
            .setProfile(Profiles.getByName(INVOICE_PROFILE))
            .setTransaction(
                build(
                    method = method,
                )
            )
    } catch (e: Exception) {
        APP_LOGGER.error("Can't init ZUGFeRD exporter!", e)
        return false
    }

    try {
        targetFile.file.outputStream().use { output ->
            exporter.export(output)
        }
    } catch (e: Exception) {
        APP_LOGGER.error("Can't export ZUGFeRD file!", e)
        return false
    }

    return true
}

class CustomZUGFeRDExporterFromPDFA : ZUGFeRDExporterFromPDFA() {
    override fun determineAndSetExporter(pdfAVersion: Int) {
        super.determineAndSetExporter(pdfAVersion)
        if (theExporter == null) {
            return
        }

        if (theExporter is ZUGFeRDExporterFromA3) {
            theExporter = object : ZUGFeRDExporterFromA3() {
                init {
                    setXMLProvider(CustomZUGFeRD2PullProvider())
                }
            }
            return
        }

        if (theExporter is ZUGFeRDExporterFromA1) {
            theExporter = object : ZUGFeRDExporterFromA1() {
                init {
                    setXMLProvider(CustomZUGFeRD2PullProvider())
                }
            }
            return
        }
    }
}

class CustomZUGFeRD2PullProvider : ZUGFeRD2PullProvider() {
    companion object {
        val elementOrderForSpecifiedTradePaymentTerms = listOf(
            "ID",
            "FromEventCode",
            "SettlementPeriodMeasure",
            "Description",
            "DueDateDateTime",
            "TypeCode",
            "InstructionTypeCode",
            "DirectDebitMandateID",
            "PartialPaymentPercent",
            "PaymentMeansID",
            "PartialPaymentAmount",
            "ApplicableTradePaymentPenaltyTerms",
            "ApplicableTradePaymentDiscountTerms",
            "PayeeTradeParty",
        )
    }

    override fun generateXML(trans: IExportableTransaction?) {
        super.generateXML(trans)

        val doc = DocumentHelper.parseText(
            zugferdData.toString(Charsets.UTF_8)
        )

        @Suppress("SpellCheckingInspection")
        val namespaceMap = mapOf(
            "ram" to "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
        )

        val xpath = doc.createXPath("//ram:SpecifiedTradePaymentTerms")
        xpath.setNamespaceURIs(namespaceMap)
        xpath.selectNodes(doc)
            .forEach { node -> fixSpecifiedTradePaymentTerms(node as Element) }

        zugferdData = XMLTools.removeBOM(
            doc.asXML().toByteArray(charset = Charsets.UTF_8)
        )
    }

    private fun fixSpecifiedTradePaymentTerms(element: Element) {
        val childElements = buildList {
            element.elements().forEach { child ->
                element.remove(child)
                add(child)
            }
        }

        elementOrderForSpecifiedTradePaymentTerms.forEach { childName ->
            childElements
                .filter { it.name == childName }
                .forEach { child ->
                    element.add(child)
                }
        }
    }
}
