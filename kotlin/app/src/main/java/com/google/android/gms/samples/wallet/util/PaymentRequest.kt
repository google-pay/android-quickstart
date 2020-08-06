package com.google.android.gms.samples.wallet.util

class PaymentRequest(
    val apiVersion: Int,
    val apiVersionMinor: Int,
    val allowedPaymentMethods: Array<TokenizationPaymentMethodSpecification<out Any, out Any?>>,
    val merchantInfo: MerchantInfo,
    val emailRequired: Boolean? = null,
    val shippingAddressRequired: Boolean? = null,
    val shippingAddressParameters: ShippingAddressParameters? = null,
    val transactionInfo: TransactionInfo,
    val shippingOptionRequired: Boolean? = null,
    val shippingOptionParameters: ShippingOptionParameters? = null
)