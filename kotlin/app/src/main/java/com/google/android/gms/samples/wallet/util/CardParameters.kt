package com.google.android.gms.samples.wallet.util

class CardParameters(
    val allowedAuthMethods: Array<CardAuthMethod>,
    val allowedCardNetworks: Array<CardNetwork>,
    val allowPrepaidCards: Boolean? = null,
    val allowCreditCards: Boolean? = null,
    val billingAddressRequired: Boolean? = null,
    val billingAddressParameters: BillingAddressParameters? = null,
    val cardNetworkParameters: CardNetworkParameters? = null
)