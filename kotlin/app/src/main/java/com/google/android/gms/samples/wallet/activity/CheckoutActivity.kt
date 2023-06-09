/*
 * Copyright 2023 Google Inc.
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

package com.google.android.gms.samples.wallet.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.samples.wallet.BuildConfig
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutBinding
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Checkout implementation for the app
 */
class CheckoutActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private lateinit var layout: ActivityCheckoutBinding

    private lateinit var payButton: Button
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use view binding to access the UI elements
        layout = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(layout.root)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        payButton = layout.payButton;
        payButton.setOnClickListener { pay() }
    }

    private fun initializePayment() {
        runBlocking {
            launch(Dispatchers.IO) {
                val (_, _, result) = BuildConfig.BACKEND_API_URL.plus("/payment_sheet")
                    .httpGet()
                    .responseJson()

                if (result is Result.Success) {
                    val responseJson = result.get().obj()
                    paymentIntentClientSecret = responseJson.getString("paymentIntent")
                    customerConfig = PaymentSheet.CustomerConfiguration(
                        responseJson.getString("customer"),
                        responseJson.getString("ephemeralKey")
                    )
                    val publishableKey = responseJson.getString("publishableKey")
                    PaymentConfiguration.init(applicationContext, publishableKey)
                }
            }
        }
    }

    private fun pay() {
        initializePayment()

        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "Domi's T-Shirt shop",
            customer = customerConfig,
            // Set `allowsDelayedPaymentMethods` to true if your business
            // can handle payment methods that complete payment after a delay, like SEPA Debit and Sofort.
            allowsDelayedPaymentMethods = true,
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "US",
                currencyCode = "USD" // Required for Setup Intents, optional for Payment Intents
            )
        )

        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            configuration
        )

    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                handleError(CommonStatusCodes.CANCELED, "Google Pay canceled")
            }

            is PaymentSheetResult.Failed -> {
                handleError(CommonStatusCodes.ERROR, paymentSheetResult.error.message)
            }

            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                startActivity(Intent(this, CheckoutSuccessActivity::class.java))
            }

        }

    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int, message: String?) {
        Log.e(TAG, "Error code: $statusCode, Message: $message")
    }
}
