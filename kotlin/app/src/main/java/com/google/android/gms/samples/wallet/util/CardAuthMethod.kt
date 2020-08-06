package com.google.android.gms.samples.wallet.util

enum class CardAuthMethod(val type: String) {
    PAN_ONLY("PAN_ONLY"),
    CRYPTOGRAM_3DS("CRYPTOGRAM_3DS")
}