/*
 * Copyright 2021 Google Inc.
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

package com.google.android.gms.samples.pay.util

import android.content.Context
import com.google.android.gms.samples.pay.Constants
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.callback.PaymentDataRequestUpdate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object PaymentsUtil {

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    /**
     * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
     *
     *
     * The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     *
     * TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     * See [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
     */
    private val gatewayTokenizationSpecification: JSONObject =
        JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put("parameters", JSONObject(Constants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS))

    /**
     * Card networks supported by your app and your gateway.
     *
     *
     * TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
     *
     * @return Allowed card networks
     * See [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private val allowedCardNetworks = JSONArray(Constants.SUPPORTED_NETWORKS)

    /**
     * Card authentication methods supported by your app and your gateway.
     *
     *
     * TODO: Confirm your processor supports Android device tokens on your supported card networks
     * and make updates in Constants.java.
     *
     * @return Allowed card authentication methods.
     * See [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private val allowedCardAuthMethods = JSONArray(Constants.SUPPORTED_METHODS)

    /**
     * Describe your app's support for the CARD payment method.
     *
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    // Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", allowedCardAuthMethods)
                .put("allowedCardNetworks", allowedCardNetworks)
                .put("billingAddressRequired", true)
                .put("billingAddressParameters", JSONObject()
                    .put("format", "FULL")
                )
            )

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification)

    val allowedPaymentMethods: JSONArray = JSONArray().put(cardPaymentMethod)

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * See [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     */
    fun isReadyToPayRequest(): JSONObject? =
        try {
            baseRequest
                .put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        } catch (e: JSONException) {
            null
        }

    /**
     * Information about the merchant requesting payment information
     *
     * @return Information about the merchant.
     * @throws JSONException
     * See [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
     */
    private val merchantInfo: JSONObject =
        JSONObject().put("merchantName", "Example Merchant")

    /**
     * Creates an instance of [PaymentsClient] for use in an [Context] using the
     * environment and theme set in [Constants].
     *
     * @param context from the caller activity.
     */
    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     * See [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
     */
    private fun getTransactionInfo(price: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("totalPriceLabel", "Total")
            .put("countryCode", Constants.COUNTRY_CODE)
            .put("currencyCode", Constants.CURRENCY_CODE)

    /**
     * shippingAddressParameters - defines the parameters for the shipping address
     */
    private val shippingAddressParameters: JSONObject =
        JSONObject()
            .put("phoneNumberRequired", false)
            .put("allowedCountryCodes", JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES))

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     * See [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/object.PaymentDataRequest)
     */
    fun getPaymentDataRequest(price: String): JSONObject =
        baseRequest
            .put("allowedPaymentMethods", allowedPaymentMethods)
            .put("transactionInfo", getTransactionInfo(price))
            .put("merchantInfo", merchantInfo)
            .put("shippingAddressRequired", true)
            .put("shippingOptionRequired", true)
            .put("shippingAddressParameters", shippingAddressParameters)
            .put("shippingOptionParameters", newShippingOptionParameters(null))
            .put("callbackIntents", JSONArray()
                .put("PAYMENT_AUTHORIZATION")
                .put("SHIPPING_ADDRESS")
                .put("SHIPPING_OPTION"))

    private val shippingOptions: Map<String, JSONObject> = mapOf(
        "shipping-001" to JSONObject()
            .put("id", "shipping-001")
            .put("label", "$0.00: Free shipping label")
            .put("description", "Free Shipping example text")
            .put("price", "0.00"),
        "shipping-002" to JSONObject()
            .put("id", "shipping-002")
            .put("label", "$1.99: Standard shipping label")
            .put("description", "Standard shipping example text.")
            .put("price", "1.99"),
        "shipping-003" to JSONObject()
            .put("id", "shipping-003")
            .put("label", "$1000: Express shipping label")
            .put("description", "Express shipping example text.")
            .put("price", "1000"),
        "shipping-004" to JSONObject()
            .put("id", "shipping-004")
            .put("label", "$2000: Same-day shipping label")
            .put("description", "Same-day shipping example text.")
            .put("price", "2000")
    )

    /**
     * newShippingOptionParameters - Encapsulated shipping option parameters (set of options)
     * definition
     */
    @Throws(JSONException::class)
    private fun newShippingOptionParameters(curShippingOptionId: String?): JSONObject {
        val shippingOptionParameters = JSONObject()
        val shippingOptionsArray = JSONArray()

        shippingOptions.values.forEach {
            shippingOptionsArray.put(
                JSONObject()
                    .put("id", it.getString("id"))
                    .put("label", it.getString("label"))
                    .put("description", it.getString("description"))
            )
        }

        shippingOptionParameters.put("shippingOptions", shippingOptionsArray)

        // set a default shipping option
        if (shippingOptions.containsKey(curShippingOptionId)) {
            shippingOptionParameters.put("defaultSelectedOptionId", curShippingOptionId)
        } else {
            shippingOptionParameters.put("defaultSelectedOptionId", "shipping-001")
        }

        return shippingOptionParameters
    }

    /**
     * createDisplayItem - Encapsulated definition for a display item
     */
    @Throws(JSONException::class)
    private fun createDisplayItem(label: String, type: String, price: String): JSONObject {
        return JSONObject().put("label", label).put("type", type).put("price", price)
    }

    /**
     * getPaymentDataRequestUpdate - Creates a PaymentDataRequestUpdate object
     */
    fun getPaymentDataRequestUpdate(
        intermediatePaymentDataJson: JSONObject,
        totalPrice: String,
        subTotal: String,
        tax: String
    ): JSONObject {
        val paymentDataRequestUpdate = JSONObject()

        // define transaction info
        val newTransactionInfo = getTransactionInfo(totalPrice)

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
                newShippingOptionParameters(shippingOptionId)
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

        // get shipping price from selected option
        val selectedShippingOption = shippingOptions[shippingOptionId]
        val shippingPrice = selectedShippingOption?.getString("price") ?: "0.00"
        if (selectedShippingOption != null) {
            displayItems.put(createDisplayItem("Shipping", "LINE_ITEM", shippingPrice))
        }

        // recalculate total price
        val newTotalPriceValue = totalPrice.toDouble() + shippingPrice.toDouble()
        newTransactionInfo.put(
            "totalPrice", String.format(Locale.getDefault(), "%.2f", newTotalPriceValue)
        )
        newTransactionInfo.put("totalPriceLabel", "Total")
        newTransactionInfo.put("displayItems", displayItems)

        // save all of the data we generated above into the appropriate objects
        paymentDataRequestUpdate.put("newTransactionInfo", newTransactionInfo)

        return paymentDataRequestUpdate
    }
}
