package com.google.android.gms.samples.wallet.util.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.gms.samples.wallet.util.CardNetwork
import com.google.android.gms.samples.wallet.util.PaymentMethodType
import com.google.android.gms.samples.wallet.util.PaymentRequest

class PaymentRequestSerializer {
    fun toJson(paymentRequest: PaymentRequest): String {
        val module = SimpleModule()
        module.addSerializer(CardNetwork::class.javaObjectType, CardNetworkSerializer())
        module.addSerializer(PaymentMethodType::class.javaObjectType, PaymentMethodTypeSerializer())

        val mapper = jacksonObjectMapper()
                .registerKotlinModule()
                .registerModule(module)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

        return mapper.writeValueAsString(paymentRequest)
    }
}