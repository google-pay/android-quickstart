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

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    data class State(
        val googleWalletAvailable: Boolean? = false,
        val googleWalletButtonClickable: Boolean = true,
        val paymentResult: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    // 1. Create the client to manage Google Pay requests

    // A client to interact with the Google Wallet API
    private val walletClient: PayClient = Pay.getClient(application)

    init {
        viewModelScope.launch {
            // 2. Define and call a method to determine whether Google Pay is available
            fetchCanAddPassesToGoogleWallet()
        }
    }

    // 2.b Define the method to call `isReadyToPay` on the client

    // 3.b Define a method to initiate the payment operation

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

    /**
     * Determine whether the API to save passes to Google Pay is available on the device.
     */
    private suspend fun fetchCanAddPassesToGoogleWallet() {
        val status = walletClient
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES).await()

        try {
            _state.update { currentState ->
                currentState.copy(googleWalletAvailable = status == PayApiAvailabilityStatus.AVAILABLE)
            }
        } catch (exception: ApiException) {
            handleError(exception.statusCode, exception.message)
        }
    }

    fun setGoogleWalletButtonClickable(clickable: Boolean) {
        _state.update { currentState ->
            currentState.copy(googleWalletButtonClickable = clickable)
        }
    }

    /**
     * Exposes the `savePassesJwt` method in the wallet client
     */
    val savePassesJwt: (String, Activity, Int) -> Unit = walletClient::savePassesJwt

    /**
     * Exposes the `savePasses` method in the wallet client
     */
    val savePasses: (String, Activity, Int) -> Unit = walletClient::savePasses

    // Test generic object used to be created against the API
    // See https://developers.google.com/wallet/tickets/boarding-passes/web#json_web_token_jwt for more details
    val genericObjectJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJnb29nbGUiLCJwYXlsb2FkIjp7ImdlbmVyaWNPYmplY3RzIjpbeyJpZCI6IjMzODgwMDAwMDAwMjIwOTUxNzcuZjUyZDRhZjYtMjQxMS00ZDU5LWFlNDktNzg2ZDY3N2FkOTJiIn1dfSwiaXNzIjoid2FsbGV0LWxhYi10b29sc0BhcHBzcG90LmdzZXJ2aWNlYWNjb3VudC5jb20iLCJ0eXAiOiJzYXZldG93YWxsZXQiLCJpYXQiOjE2NTA1MzI2MjN9.ZURFHaSiVe3DfgXghYKBrkPhnQy21wMR9vNp84azBSjJxENxbRBjqh3F1D9agKLOhrrflNtIicShLkH4LrFOYdnP6bvHm6IMFjqpUur0JK17ZQ3KUwQpejCgzuH4u7VJOP_LcBEnRtzZm0PyIvL3j5-eMRyRAo5Z3thGOsKjqCPotCAk4Z622XHPq5iMNVTvcQJaBVhmpmjRLGJs7qRp87sLIpYOYOkK8BD7OxLmBw9geqDJX-Y1zwxmQbzNjd9z2fuwXX66zMm7pn6GAEBmJiqollFBussu-QFEopml51_5nf4JQgSdXmlfPrVrwa6zjksctIXmJSiVpxL7awKN2w"
}