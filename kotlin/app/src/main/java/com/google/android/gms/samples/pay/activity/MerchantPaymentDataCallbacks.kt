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
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks
import com.google.android.gms.wallet.callback.IntermediatePaymentData
import com.google.android.gms.wallet.callback.OnCompleteListener
import com.google.android.gms.wallet.callback.PaymentAuthorizationResult
import com.google.android.gms.wallet.callback.PaymentDataRequestUpdate
import org.json.JSONArray
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
        val totalPrice = "2.9"
        val subTotal = "1.0"
        val tax = "0.0"
        var shippingPrice = "0.0"
        val paymentDataRequestUpdate = JSONObject()
        val newSavedState = Bundle()

        try {
            val intermediatePaymentDataJson = JSONObject(request?.toJson() ?: "{}")

            // define transaction info
            val newTransactionInfo = JSONObject().apply {
                put("currencyCode", "USD")
                put("totalPriceLabel", "FINAL")
                put("totalPrice", totalPrice)
            }

            // process user-provided shipping option data
            var shippingOptionId: String? = null
            if (intermediatePaymentDataJson.has("shippingOptionData")
                && intermediatePaymentDataJson.getJSONObject("shippingOptionData").has("id")
            ) {
                shippingOptionId = intermediatePaymentDataJson.getJSONObject("shippingOptionData").getString("id")
            }

            // define shipping options
            if (intermediatePaymentDataJson.has("shippingAddress")) {
                paymentDataRequestUpdate.put(
                    "newShippingOptionParameters",
                    newShippingOptionParams(shippingOptionId)
                )
                if (paymentDataRequestUpdate.has("newShippingOptionParameters")) {
                    shippingOptionId = paymentDataRequestUpdate
                        .getJSONObject("newShippingOptionParameters")
                        .getString("defaultSelectedOptionId")
                }
            }

            // define displayItems
            val displayItems = JSONArray()
            displayItems.put(createDisplayItem("Subtotal", "SUBTOTAL", subTotal))
            displayItems.put(createDisplayItem("Estimated tax", "TAX", tax))

            // get shipping data
            val shippingData = getShippingData(shippingOptionId)

            // define shipping price
            if (shippingData.has("price")) {
                shippingPrice = shippingData.getString("price")
                displayItems.put(shippingData) // and data display item
            }

            // recalculate total price
            val newTotalPriceValue = totalPrice.toDouble() + shippingPrice.toDouble()
            newTransactionInfo.put(
                "totalPrice", String.format(Locale.getDefault(), "%.2f", newTotalPriceValue)
            )

            // save all of the data we generated above into the appropriate objects
            paymentDataRequestUpdate.put("newTransactionInfo", newTransactionInfo)
            newSavedState.putString("paymentDataRequestUpdate", paymentDataRequestUpdate.toString())

            // return the generated data to the client
            onCompleteListener.complete(
                PaymentDataRequestUpdate.fromJson(paymentDataRequestUpdate.toString())
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

    companion object {
        /**
         * newShippingOptionParams - Encapsulated shipping option parameters (set of options)
         * definition
         */
        @Throws(JSONException::class)
        private fun newShippingOptionParams(curShippingOptionId: String?): JSONObject {
            val shippingOptionParameters = JSONObject()
            val shippingOptions = JSONArray()

            shippingOptions.put(
                createShippingOption(
                    "shipping-001",
                    "$0.00: Free shipping label",
                    "Free Shipping example text"
                )
            )
            shippingOptions.put(
                createShippingOption(
                    "shipping-002",
                    "$1.99: Standard shipping label",
                    "Standard shipping example text."
                )
            )
            shippingOptions.put(
                createShippingOption(
                    "shipping-003",
                    "$1000: Express shipping label",
                    "Express shipping example text."
                )
            )
            shippingOptions.put(
                createShippingOption(
                    "shipping-004",
                    "$2000: Same-day shipping label",
                    "Same-day shipping example text."
                )
            )

            shippingOptionParameters.put("shippingOptions", shippingOptions)

            val shippingOptionIds = setOf(
                "shipping-001", "shipping-002", "shipping-003", "shipping-004"
            )

            // set a default shipping option
            if (shippingOptionIds.contains(curShippingOptionId)) {
                shippingOptionParameters.put("defaultSelectedOptionId", curShippingOptionId)
            } else {
                shippingOptionParameters.put("defaultSelectedOptionId", "shipping-001")
            }

            return shippingOptionParameters
        }

        /**
         * createShippingOption - Defines an encapsulated shipping option
         */
        @Throws(JSONException::class)
        private fun createShippingOption(
            id: String,
            label: String,
            description: String
        ): JSONObject {
            return JSONObject().put("id", id).put("label", label).put("description", description)
        }

        /**
         * createDisplayItem - Encapsulated definition for a display item
         */
        @Throws(JSONException::class)
        private fun createDisplayItem(label: String, type: String, price: String): JSONObject {
            return JSONObject().put("label", label).put("type", type).put("price", price)
        }

        @Throws(JSONException::class)
        fun getShippingData(shippingOptionId: String?): JSONObject {
            // example of how to provide different shipping data depending on user-selected option
            return when (shippingOptionId) {
                "shipping-001" -> createDisplayItem("Shipping", "LINE_ITEM", "0")
                "shipping-002" -> createDisplayItem("Shipping", "LINE_ITEM", "1.99")
                "shipping-003" -> createDisplayItem("Shipping", "LINE_ITEM", "1000")
                "shipping-004" -> createDisplayItem("Shipping", "LINE_ITEM", "2000")
                "shipping_option_unselected" -> JSONObject()
                else -> throw JSONException("This shipping option is invalid for the given address")
            }
        }
    }
}
