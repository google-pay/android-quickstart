package com.google.android.gms.samples.wallet.util

class PaymentGatewayTokenizationSpecification(
    override val parameters: Any
) : TokenizationSpecification<Any>() {
    override val type: PaymentMethodTokenizationType = PaymentMethodTokenizationType.PAYMENT_GATEWAY
}