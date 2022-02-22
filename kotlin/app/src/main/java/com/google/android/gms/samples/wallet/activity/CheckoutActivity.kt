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

package com.google.android.gms.samples.wallet.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutBinding
import com.google.android.gms.samples.wallet.viewmodel.CheckoutViewModel
import com.google.android.gms.wallet.*
import org.json.JSONException
import org.json.JSONObject


/**
 * Checkout implementation for the app
 */
class CheckoutActivity : AppCompatActivity() {

    private val model: CheckoutViewModel by viewModels()

    private lateinit var layoutBinding: ActivityCheckoutBinding
    private lateinit var googlePayButton: View

    // Handle potential conflict from calling loadPaymentData.
    private val resolvePaymentForResult = registerForActivityResult(StartIntentSenderForResult()) {
        result: ActivityResult ->
        when (result.resultCode) {
            RESULT_OK ->
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                }

            RESULT_CANCELED -> {
                // The user cancelled the payment attempt
            }
        }
    }

    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see AppCompatActivity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use view binding to access the UI elements
        layoutBinding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        googlePayButton = layoutBinding.googlePayButton.root
        googlePayButton.setOnClickListener { requestPayment() }

        // Check whether Google Pay can be used to complete a payment
        model.canUseGooglePay.observe(this, Observer(::setGooglePayAvailable))
    }

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                    this,
                    R.string.googlepay_status_unavailable,
                    Toast.LENGTH_LONG).show()
        }
    }
    
    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val dummyPriceCents = 100L
        val shippingCostCents = 900L
        val task = model.getLoadPaymentDataTask(dummyPriceCents + shippingCostCents)

        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                completedTask.result.let(::handlePaymentSuccess)
            } else {
                when (val exception = completedTask.exception) {
                    is ResolvableApiException -> {
                        resolvePaymentForResult.launch(
                                IntentSenderRequest.Builder(exception.resolution).build())
                    }
                    is ApiException -> {
                        handleError(exception.statusCode, exception.message)
                    }
                    else -> {
                        handleError(CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                " exception when trying to deliver the task result to an activity!")
                    }
                }
            }

            // Re-enables the Google Pay payment button.
            googlePayButton.isClickable = true
        }
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
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                    .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show()

            // Logging token string.
            Log.d("Google Pay token", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token"))

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
        Log.w("loadPaymentData failed", "Error code: $statusCode, Message: $message")
    }
}
