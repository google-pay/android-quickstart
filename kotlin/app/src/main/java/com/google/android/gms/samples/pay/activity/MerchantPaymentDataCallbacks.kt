/*
 * Copyright 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.pay.activity

import android.os.Bundle
import android.util.Log
import com.google.android.gms.samples.pay.Constants
import com.google.android.gms.samples.pay.util.PaymentsUtil
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks
import com.google.android.gms.wallet.callback.IntermediatePaymentData
import com.google.android.gms.wallet.callback.OnCompleteListener
import com.google.android.gms.wallet.callback.PaymentAuthorizationResult
import com.google.android.gms.wallet.callback.PaymentDataRequestUpdate
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class MerchantPaymentDataCallbacks : BasePaymentDataCallbacks() {

    /**
     * onPaymentDataChanged callback - Handles payment data changes in the payment sheet such as
     * shipping address and shipping options.
     */
    override fun onPaymentDataChanged(
        request: IntermediatePaymentData?,
        onCompleteListener: OnCompleteListener<PaymentDataRequestUpdate>
    ) {
        // define prices and variables
        val subTotal = Constants.PAYMENT_SUBTOTAL
        val tax = Constants.PAYMENT_TAX
        val totalPrice = String.format(Locale.getDefault(), "%.2f", subTotal.toDouble() + tax.toDouble())
        val newSavedState = Bundle()

        try {
            val intermediatePaymentDataJson = JSONObject(request?.toJson() ?: "{}")

            val paymentDataRequestUpdateJson = PaymentsUtil.getPaymentDataRequestUpdate(
                intermediatePaymentDataJson, totalPrice, subTotal, tax
            )

            newSavedState.putString("paymentDataRequestUpdate", paymentDataRequestUpdateJson.toString())

            // return the generated data to the client
            onCompleteListener.complete(
                PaymentDataRequestUpdate.fromJson(paymentDataRequestUpdateJson.toString())
                    .withUpdatedSavedState(newSavedState)
            )

        } catch (e: JSONException) {
            Log.e("MerchantPaymentDataCallbacks", e.message, e)
            throw RuntimeException(e)
        }
    }

    /**
     * onPaymentAuthorized callback - Called when a payment is authorized in the payment sheet.
     */
    override fun onPaymentAuthorized(
        request: PaymentData?,
        onCompleteListener: OnCompleteListener<PaymentAuthorizationResult>
    ) {
        Log.i("Invocation", "onPaymentAuthorized invoked")
        val savedState = Bundle()
        val paymentAuthorizationResultJson = JSONObject()

        try {
            val paymentDataJson = JSONObject(request?.toJson() ?: "{}")
            val info = paymentDataJson.getJSONObject("paymentMethodData").getJSONObject("info")

            // example of how to check for a card network
            val cardNetwork = info.getString("cardNetwork")
            if (cardNetwork == "VISA") {
                paymentAuthorizationResultJson.put("transactionState", "SUCCESS")
            } else {
                paymentAuthorizationResultJson.put("transactionState", "ERROR")
                val error = JSONObject().apply {
                    put("reason", "PAYMENT_DATA_INVALID")
                    put("message", "Only Visa is accepted!")
                    put("intent", "PAYMENT_AUTHORIZATION")
                }
                paymentAuthorizationResultJson.put("error", error)
            }

            onCompleteListener.complete(
                PaymentAuthorizationResult.fromJson(paymentAuthorizationResultJson.toString())
                    .withUpdatedSavedState(savedState)
            )

        } catch (e: JSONException) {
            Log.e("MerchantPaymentDataCallbacks", e.message, e)
            throw RuntimeException(e)
        }
    }
}
