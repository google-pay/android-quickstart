package com.google.android.gms.samples.wallet.util

class TransactionInfo(
    val currencyCode: String,
    val countryCode: String,
    val totalPrice: String,
    val totalPriceStatus: TotalPriceStatus,
    val checkoutOption: CheckoutOption? = null,
    val displayItems: Array<DisplayItem>? = null
)