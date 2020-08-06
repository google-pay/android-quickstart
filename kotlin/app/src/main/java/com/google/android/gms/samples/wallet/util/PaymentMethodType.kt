package com.google.android.gms.samples.wallet.util

class PaymentMethodType(val type: String) {
    companion object {
        val CARD = PaymentMethodType("CARD")
        val PAYPAL = PaymentMethodType("PAYPAL")
    }

    override fun toString(): String {
        return type
    }
}