package de.openindex.zugferd.manager.model

enum class PaymentMethod(
    val code: Int,
    val description: String
) {
    /**
     * In Cash.
     */
    //CASH(10, "Barzahlung"),

    /**
     * Credit Transfer.
     */
    //CREDIT_TRANSFER(30, "Überweisung"),

    /**
     * Debit Transfer.
     */
    //DEBIT_TRANSFER(31, "Bankeinzug"),

    /**
     * Payment to bank account.
     */
    //PAYMENT_TO_BANK_ACCOUNT(42, "Einzahlung auf Konto"),

    /**
     * Bank card.
     */
    //BANK_CARD(48, "Bank-Karte"),

    /**
     * Direct Debit.
     */
    //DIRECT_DEBIT(49, "Abbuchung"),

    /**
     * Standing agreement.
     */
    //STANDING_AGREEMENT(57, "Abkommen"),

    /**
     * SEPA credit transfer.
     */
    SEPA_CREDIT_TRANSFER(58, "SEPA-Überweisung"),

    /**
     * SEPA direct debit.
     */
    SEPA_DIRECT_DEBIT(59, "SEPA-Lastschrift"),

    /**
     * Clearing between partners
     */
    //CLEARING_BETWEEN_PARTNERS(97, "partnerschaftliche Verrechnung"),

    ;
}
