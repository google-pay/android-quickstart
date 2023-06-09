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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.samples.wallet.BuildConfig
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutBinding
import com.google.android.gms.samples.wallet.util.PaymentsUtil
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Checkout implementation for the app
 */
class CheckoutActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private lateinit var layout: ActivityCheckoutBinding
    private lateinit var googlePayButton: PayButton
    private lateinit var paymentIntentClientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use view binding to access the UI elements
        layout = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(layout.root)

        googlePayButton = layout.googlePayButton

        googlePayButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonType(ButtonType.BUY)
                .setAllowedPaymentMethods(PaymentsUtil.allowedPaymentMethods.toString()).build()
        )

        val googlePayLauncher = GooglePayLauncher(
            activity = this,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test,
                merchantCountryCode = "US",
                merchantName = "Google Pay showcase"
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )

        googlePayButton.setOnClickListener {pay(googlePayLauncher)  }

    }

    private fun initializePayment() {
        runBlocking {
            launch(Dispatchers.IO) {
                val (_, _, result) = BuildConfig.BACKEND_API_URL.plus("/payment_intent")
                    .httpGet()
                    .responseJson()

                if (result is Result.Success) {
                    val responseJson = result.get().obj()
                    paymentIntentClientSecret = responseJson.getString("paymentIntent")
                    val publishableKey = responseJson.getString("publishableKey")
                    PaymentConfiguration.init(applicationContext, publishableKey)
                }
            }
        }
    }

    private fun pay(googlePayLauncher: GooglePayLauncher) {
        initializePayment()
        googlePayLauncher.presentForPaymentIntent(paymentIntentClientSecret)
    }

    private fun onGooglePayReady(isReady: Boolean) {
        if(isReady) {
            googlePayButton.visibility = View.VISIBLE
            googlePayButton.isClickable = true
        }

    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                startActivity(Intent(this, CheckoutSuccessActivity::class.java))
            }
            GooglePayLauncher.Result.Canceled -> {
                handleError(CommonStatusCodes.CANCELED, "Google Pay canceled")
            }
            is GooglePayLauncher.Result.Failed -> {
                // Operation failed; inspect `result.error` for the exception
                handleError(CommonStatusCodes.ERROR, result.error.message)
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
