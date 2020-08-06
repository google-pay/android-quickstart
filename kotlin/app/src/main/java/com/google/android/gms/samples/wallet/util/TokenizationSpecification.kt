package com.google.android.gms.samples.wallet.util

abstract class TokenizationSpecification<T> {
    abstract val type: PaymentMethodTokenizationType
    abstract val parameters: T
}
