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

package com.google.android.gms.samples.pay.util

import android.content.Context
import com.google.android.gms.samples.pay.Constants
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Locale

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * <p>Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object PaymentsUtil {

    /**
     * Encapsulated shipping option parameters (set of options) definition.
     *
     * @return A {@link JSONObject} containing shipping options and the default selected option.
     * @throws JSONException If the JSON object is malformed.
     */
    @Throws(JSONException::class)
    fun getShippingOptionParameters(): JSONObject {
        val shippingOptionParameters = JSONObject()
        val shippingOptions = JSONArray()

        shippingOptions.put(
            createShippingOption(
                "shipping-001", "$0.00: Free shipping label", "Free Shipping example text"
            )
        )
        shippingOptions.put(
            createShippingOption(
                "shipping-002", "$1.99: Standard shipping label", "Standard shipping example text."
            )
        )
        shippingOptions.put(
            createShippingOption(
                "shipping-003", "$1000: Express shipping label", "Express shipping example text."
            )
        )
        shippingOptions.put(
            createShippingOption(
                "shipping-004", "$2000: Same-day shipping label", "Same-day shipping example text."
            )
        )

        shippingOptionParameters.put("shippingOptions", shippingOptions)
        shippingOptionParameters.put("defaultSelectedOptionId", "shipping-001")

        return shippingOptionParameters
    }

    /**
     * Defines an encapsulated shipping option.
     *
     * @param id The unique identifier for the shipping option.
     * @param label The label to display for the shipping option.
     * @param description A brief description of the shipping option.
     * @return A {@link JSONObject} representing the shipping option.
     * @throws JSONException If the JSON object is malformed.
     */
    @Throws(JSONException::class)
    private fun createShippingOption(id: String, label: String, description: String): JSONObject {
        return JSONObject().put("id", id).put("label", label).put("description", description)
    }

    /**
     * Encapsulated definition for a display item.
     *
     * @param label The label to display for the item.
     * @param type The type of the display item (e.g., LINE_ITEM, SUBTOTAL).
     * @param price The price of the item.
     * @return A {@link JSONObject} representing the display item.
     * @throws JSONException If the JSON object is malformed.
     */
    @Throws(JSONException::class)
    fun createDisplayItem(label: String, type: String, price: String): JSONObject {
        return JSONObject().put("label", label).put("type", type).put("price", price)
    }

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException if the object is malformed.
     */
    @Throws(JSONException::class)
    private fun getBaseRequest(): JSONObject {
        return JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0)
    }

    /**
     * Creates an instance of {@link PaymentsClient} for use in an {@link Context} using the
     * environment and theme set in {@link Constants}.
     *
     * @param context is the caller's context.
     * @return An instance of {@link PaymentsClient}.
     */
    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
            .build()
        return Wallet.getPaymentsClient(context, walletOptions)
    }

    /**
     * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
     *
     * <p>The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     * <p>TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
     */
    @Throws(JSONException::class)
    private fun getGatewayTokenizationSpecification(): JSONObject {
        return JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put(
                "parameters",
                JSONObject()
                    .put("gateway", "example")
                    .put("gatewayMerchantId", "exampleGatewayMerchantId")
            )
    }

    /**
     * {@code DIRECT} Integration: Decrypt a response directly on your servers. This configuration has
     * additional data security requirements from Google and additional PCI DSS compliance complexity.
     *
     * <p>Please refer to the documentation for more information about {@code DIRECT} integration. The
     * type of integration you use depends on your payment processor.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
     */
    @Throws(JSONException::class, RuntimeException::class)
    private fun getDirectTokenizationSpecification(): JSONObject {
        return JSONObject()
            .put("type", "DIRECT")
            .put("parameters", JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS))
    }

    /**
     * Card networks supported by your app and your gateway.
     *
     * <p>TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
     *
     * @return Allowed card networks.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
     */
    private fun getAllowedCardNetworks(): JSONArray {
        return JSONArray(Constants.SUPPORTED_NETWORKS)
    }

    /**
     * Card authentication methods supported by your app and your gateway.
     *
     * <p>TODO: Confirm your processor supports Android device tokens on your supported card networks
     * and make updates in Constants.java.
     *
     * @return Allowed card authentication methods.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
     */
    private fun getAllowedCardAuthMethods(): JSONArray {
        return JSONArray(Constants.SUPPORTED_METHODS)
    }

    /**
     * Describe your app's support for the CARD payment method.
     *
     * <p>The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
     */
    @Throws(JSONException::class)
    private fun getBaseCardPaymentMethod(): JSONObject {
        return JSONObject()
            .put("type", "CARD")
            .put(
                "parameters",
                JSONObject()
                    .put("allowedAuthMethods", getAllowedCardAuthMethods())
                    .put("allowedCardNetworks", getAllowedCardNetworks())
                    .put("billingAddressRequired", true)
                    .put("billingAddressParameters", JSONObject().put("format", "FULL"))
            )
    }

    /**
     * Describe the expected returned payment data for the CARD payment method.
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
     */
    @Throws(JSONException::class)
    private fun getCardPaymentMethod(): JSONObject {
        return getBaseCardPaymentMethod()
            .put("tokenizationSpecification", getGatewayTokenizationSpecification())
    }

    /**
     * Return a collection of payment methods allowed to complete the operation with Google Pay.
     *
     * @return A JSONArray object with the list of payment methods.
     * @throws JSONException if the JSON object is malformed.
     */
    @Throws(JSONException::class)
    fun getAllowedPaymentMethods(): JSONArray {
        return JSONArray().put(getCardPaymentMethod())
    }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#IsReadyToPayRequest">IsReadyToPayRequest</a>
     */
    @Throws(JSONException::class)
    fun getIsReadyToPayRequest(): JSONObject {
        return getBaseRequest()
            .put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @param price The price of the product.
     * @return information about the requested payment.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
     */
    @Throws(JSONException::class)
    fun getTransactionInfo(price: String): JSONObject {
        return JSONObject()
            .put("totalPrice", price)
            .put("totalPriceLabel", "Total")
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", Constants.COUNTRY_CODE)
            .put("currencyCode", Constants.CURRENCY_CODE)
            .put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE")
            .put("displayItems", getDisplayItems(price))
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @param price The price of the product.
     * @return information about the requested payment.
     * @throws JSONException if the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
     */
    @Throws(JSONException::class)
    fun getDisplayItems(price: String): JSONArray {
        val displayItems = JSONArray()
        val tax = String.format(
            Locale.getDefault(), "%.2f", BigDecimal(price).multiply(BigDecimal(Constants.TAX_RATE.toString()))
        )
        displayItems.put(createDisplayItem("Total", "SUBTOTAL", price))
        displayItems.put(createDisplayItem("Tax", "TAX", tax))
        return displayItems
    }

    /**
     * An object describing information to be requested via the Google Pay payment sheet.
     *
     * @param priceLabel the price of the product
     * @return Payment data expected by your app.
     * @throws JSONException If the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
     */
    @Throws(JSONException::class)
    fun getPaymentDataRequest(priceLabel: String): JSONObject {
        return getBaseRequest()
            .put("allowedPaymentMethods", getAllowedPaymentMethods())
            .put("transactionInfo", getTransactionInfo(priceLabel))
            .put("merchantInfo", JSONObject().put("merchantName", Constants.MERCHANT_NAME))
            .put("shippingAddressRequired", true)
            .put("shippingOptionRequired", true)
            .put("shippingOptionParameters", getShippingOptionParameters())
            .put(
                "shippingAddressParameters",
                JSONObject()
                    .put("phoneNumberRequired", false)
                    .put("allowedCountryCodes", JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES))
            )
            .put(
                "callbackIntents",
                JSONArray()
                    .put("PAYMENT_AUTHORIZATION")
                    .put("SHIPPING_ADDRESS")
                    .put("SHIPPING_OPTION")
            )
    }

    /**
     * An object describing information to be updated via the Google Pay payment sheet.
     *
     * @param intermediatePaymentData the intermediate payment data containing user selections.
     * @param priceLabel the price of the product.
     * @return Payment data expected by your app.
     * @throws JSONException If the object is malformed.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
     */
    @Throws(JSONException::class)
    fun getPaymentDataRequestUpdate(
        intermediatePaymentData: JSONObject, priceLabel: String
    ): JSONObject {
        // Populate the payment request with default data
        val paymentDataRequestUpdate = JSONObject()
        paymentDataRequestUpdate.put("newTransactionInfo", getTransactionInfo(priceLabel))

        val shippingOptionParameters = getShippingOptionParameters()
        paymentDataRequestUpdate.put("newShippingOptionParameters", shippingOptionParameters)

        // Update the selected shippingOption based on the user selection
        var shippingOptionId = "shipping-001"
        if (intermediatePaymentData.has("shippingOptionData")
            && intermediatePaymentData.getJSONObject("shippingOptionData").has("id")
        ) {
            shippingOptionId =
                intermediatePaymentData.getJSONObject("shippingOptionData").getString("id")
            paymentDataRequestUpdate
                .getJSONObject("newShippingOptionParameters")
                .put("defaultSelectedOptionId", shippingOptionId)
        }
        // Get display item for the selected shipping method and add it to paymentDataRequestUpdate
        val shippingDisplayItem = getShippingDisplayItem(shippingOptionId)
        paymentDataRequestUpdate
            .getJSONObject("newTransactionInfo")
            .getJSONArray("displayItems")
            .put(shippingDisplayItem)

        // define shipping price
        if (shippingDisplayItem.has("price")) {
            // Update displayItems with the new price.
            val totalPrice =
                paymentDataRequestUpdate.getJSONObject("newTransactionInfo").getString("totalPrice")
            val shippingPrice = shippingDisplayItem.getString("price")
            val newTotalPriceValue = BigDecimal(totalPrice).add(BigDecimal(shippingPrice))
            paymentDataRequestUpdate
                .getJSONObject("newTransactionInfo")
                .put("totalPrice", String.format(Locale.getDefault(), "%.2f", newTotalPriceValue))
        }
        return paymentDataRequestUpdate
    }

    /**
     * Get a display item object for the selected shipping option.
     *
     * @param shippingOptionId the ID of the selected shipping option.
     * @return a JSONObject containing the display item for the shipping option.
     * @throws JSONException if the shipping option is invalid.
     */
    @Throws(JSONException::class)
    private fun getShippingDisplayItem(shippingOptionId: String?): JSONObject {
        if (shippingOptionId == null) {
            return JSONObject()
        }

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
