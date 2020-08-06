package com.google.android.gms.samples.wallet.util.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.android.gms.samples.wallet.util.CardNetwork

internal class CardNetworkSerializer(t: Class<CardNetwork>?) : StdSerializer<CardNetwork>(t) {
    constructor() : this(null) {
    }

    override fun serialize(value: CardNetwork?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeString(value?.toString())
    }
}