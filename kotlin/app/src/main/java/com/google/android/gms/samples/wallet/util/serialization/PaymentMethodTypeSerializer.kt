package com.google.android.gms.samples.wallet.util.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.android.gms.samples.wallet.util.PaymentMethodType

internal class PaymentMethodTypeSerializer(t: Class<PaymentMethodType>?) : StdSerializer<PaymentMethodType>(t) {
    constructor() : this(null) {
    }

    override fun serialize(value: PaymentMethodType?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeString(value?.toString())
    }
}