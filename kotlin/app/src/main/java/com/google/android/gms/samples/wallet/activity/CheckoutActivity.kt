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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.ui.ProductScreen
import com.google.android.gms.samples.wallet.viewmodel.CheckoutViewModel

class CheckoutActivity : ComponentActivity() {

    private val addToGoogleWalletRequestCode = 1000
    private val model: CheckoutViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProductScreen(
                title = "Men's Tech Shell Full-Zip",
                description = "A versatile full-zip that you can wear all day long and even...",
                price = "$50.20",
                image = R.drawable.ts_10_11019a,
                viewModel = model,
                googlePayButtonOnClick = { requestPayment() },
                googleWalletButtonOnClick = { requestSavePass() },
            )
        }
    }

    private fun requestPayment() {
        // Disables the button to prevent multiple clicks.
        model.setGooglePayButtonClickable(false)

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val dummyPriceCents = 100L
        val shippingCostCents = 900L
        val task = model.getLoadPaymentDataTask(dummyPriceCents + shippingCostCents)

    }


    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson()

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            Toast.makeText(
                this,
                getString(R.string.payments_show_name, billingName),
                Toast.LENGTH_LONG
            ).show()

            // Logging token string.
            Log.d(
                "Google Pay token", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
            )

            model.checkoutSuccess()

        } catch (error: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $error")
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
        Log.e("Google Pay API error", "Error code: $statusCode, Message: $message")
    }

    private fun requestSavePass() {

        // Disables the button to prevent multiple clicks.
        model.setGoogleWalletButtonClickable(false)

        model.savePassesJwt(model.genericObjectJwt, this, addToGoogleWalletRequestCode)
    }

    @Deprecated("Deprecated and in use by Google Pay")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == addToGoogleWalletRequestCode) {
            when (resultCode) {
                RESULT_OK -> Toast
                    .makeText(
                        this,
                        getString(R.string.add_google_wallet_success),
                        Toast.LENGTH_LONG
                    )
                    .show()

                RESULT_CANCELED -> {
                    // Save canceled
                }

                PayClient.SavePassesResult.SAVE_ERROR -> data?.let { intentData ->
                    val apiErrorMessage =
                        intentData.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE)
                    handleError(resultCode, apiErrorMessage)
                }

                else -> handleError(
                    CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                            " exception when trying to deliver the task result to an activity!"
                )
            }

            // Re-enables the Google Pay payment button.
            model.setGoogleWalletButtonClickable(true)

        }
    }
}