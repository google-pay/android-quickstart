package com.google.android.gms.samples.wallet.util

class CardNetwork(val network: String) {
    companion object {
        val AMEX = CardNetwork("AMEX")
        val DISCOVER = CardNetwork("DISCOVER")
        val ELECTRON = CardNetwork("ELECTRON")
        val ELO = CardNetwork("ELO")
        val ELO_DEBIT = CardNetwork("ELO_DEBIT")
        val INTERAC = CardNetwork("INTERAC")
        val JCB = CardNetwork("JCB")
        val MAESTRO = CardNetwork("MAESTRO")
        val MASTERCARD = CardNetwork("MASTERCARD")
        val VISA = CardNetwork("VISA")
    }

    override fun toString(): String {
        return network
    }
}