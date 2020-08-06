package com.google.android.gms.samples.wallet.util

class DirectTokenizationSpecification(
    override val parameters: DirectTokenizationParameters? = null
) : TokenizationSpecification<DirectTokenizationParameters?>() {
    override val type: PaymentMethodTokenizationType = PaymentMethodTokenizationType.DIRECT

    constructor(protocolVersion: String, publicKey: String) : this(DirectTokenizationParameters(protocolVersion, publicKey))
}