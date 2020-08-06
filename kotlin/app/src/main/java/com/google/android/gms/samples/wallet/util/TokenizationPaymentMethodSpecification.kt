package com.google.android.gms.samples.wallet.util

class TokenizationPaymentMethodSpecification<TParameterType, TSpecificationType>(
    type: PaymentMethodType,
    parameters: TParameterType,
    val tokenizationSpecification: TokenizationSpecification<TSpecificationType>
) : PaymentMethodSpecification<TParameterType>(type, parameters)