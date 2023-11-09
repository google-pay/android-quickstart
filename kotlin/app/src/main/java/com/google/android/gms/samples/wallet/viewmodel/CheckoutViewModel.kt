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

package com.google.android.gms.samples.wallet.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val _paymentUiState: MutableStateFlow<PaymentUiState> = MutableStateFlow(PaymentUiState.NotStarted)
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

    // 1. Create the client to manage Google Pay requests

    init {
        viewModelScope.launch {
            // 2. Define and call a method to determine whether Google Pay is available
        }
    }

    // 2.b Define the method to call `isReadyToPay` on the client

    // 4.0 Define a method that retrieves the loadPaymentData task

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

    // 4.c Define a method that receives a payment data object

    private fun extractPaymentBillingName(paymentData: PaymentData): String? {
            val paymentInformation = paymentData.toJson()

            try {
                // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
                val paymentMethodData =
                    JSONObject(paymentInformation).getJSONObject("paymentMethodData")
                val billingName = paymentMethodData.getJSONObject("info")
                    .getJSONObject("billingAddress").getString("name")
                Log.d("BillingName", billingName)

                // Logging token string.
                Log.d(
                    "Google Pay token", paymentMethodData
                        .getJSONObject("tokenizationData")
                        .getString("token")
                )

                return billingName
            } catch (error: JSONException) {
                Log.e("handlePaymentSuccess", "Error: $error")
            }

            return null
        }
}

// 3. Review the payment result class
sealed interface PaymentUiState {
    object NotStarted : PaymentUiState
    object Available : PaymentUiState
    data class PaymentCompleted(val paymentData: PaymentResult) : PaymentUiState
    data class Error(val code: Int, val message: String? = null) : PaymentUiState
}

data class PaymentResult(val billingName: String)