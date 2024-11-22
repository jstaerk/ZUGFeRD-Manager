package de.openindex.zugferd.manager.model

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

const val DEFAULT_CURRENCY = "EUR"

@Serializable
data class Invoice(
    @Suppress("PropertyName")
    val _paymentMethod: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,

    val number: String = "",
    val issueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val dueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(DatePeriod(days = 14)),
    val sender: TradeParty? = null,
    val recipient: TradeParty? = null,
    val currency: String = DEFAULT_CURRENCY,
    val items: List<Item> = listOf(),
)

//expect fun Invoice.isValid(): Boolean

fun Invoice.isValid(): Boolean {
    return number.isNotBlank()
            //&& issueDate != null
            && sender != null
            && sender.name.isNotBlank()
            && (sender.vatID != null || sender.taxID != null)
            && recipient != null
            && recipient.name.isNotBlank()
}

expect fun Invoice.toXml(
    method: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,
): String

expect suspend fun Invoice.export(
    sourceFile: PlatformFile,
    targetFile: PlatformFile,
    method: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,
): Boolean
