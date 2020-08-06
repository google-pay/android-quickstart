package com.google.android.gms.samples.wallet.util

class CardNetworkParameters(
    val cardNetwork: CardNetwork,
    val acquirerBin: String? = null,
    val acquirerMerchantId: String? = null
)