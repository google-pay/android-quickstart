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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.util.PaymentsUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.*

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    // A client for interacting with the Google Pay API.
    private val paymentsClient: PaymentsClient = PaymentsUtil.createPaymentsClient(application)

    // LiveData with the result of whether the user can pay using Google Pay
    private val _canUseGooglePay: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanUseGooglePay()
        }
    }

    val canUseGooglePay: LiveData<Boolean> = _canUseGooglePay

    /**
     * Determine the user's ability to pay with a payment method supported by your app and display
     * a Google Pay payment button.
     *
     * @return a [LiveData] object that holds the future result of the call.
     * @see [](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html.isReadyToPay)
    ) */
    private fun fetchCanUseGooglePay() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest()
        if (isReadyToPayJson == null) _canUseGooglePay.value = false

        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                _canUseGooglePay.value = completedTask.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                Log.w("isReadyToPay failed", exception)
                _canUseGooglePay.value = false
            }
        }
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @param priceCents the price to show on the payment sheet.
     * @return a [Task] with the payment information.
     * @see [](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest)
    ) */
    fun getLoadPaymentDataTask(priceCents: Long): Task<PaymentData> {
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(request)
    }
}