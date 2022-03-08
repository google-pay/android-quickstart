package com.google.android.gms.samples.wallet.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import com.google.android.gms.samples.wallet.util.PaymentsUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.*
import com.google.android.gms.wallet.wobs.LoyaltyPoints
import com.google.android.gms.wallet.wobs.LoyaltyPointsBalance
import com.google.android.gms.wallet.wobs.WalletObjectsConstants

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val passesIssuerId = "3388000000022095177"

    // A client for interacting with the Google Pay API.
    private val paymentsClient: PaymentsClient = PaymentsUtil.createPaymentsClient(application)

    // A client to interact with the Google Pay Passes API
    private val passesPayClient: PayClient = Pay.getClient(application)

    // LiveData with the result of whether the user can pay using Google Pay
    private val _canUseGooglePay: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanUseGooglePay()
        }
    }

    // LiveData with the result of whether the user can save passes to Google Pay
    private val _canSavePasses: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanSavePassesToGooglePay()
        }
    }

    val canUseGooglePay: LiveData<Boolean> = _canUseGooglePay
    val canSavePasses: LiveData<Boolean> = _canSavePasses

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

    /**
     * Determine whether the API to save passes to Google Pay is available on the device.
     *
     * @return a [LiveData] object that holds the future result of the call.
    ) */
    private fun fetchCanSavePassesToGooglePay() {
        passesPayClient
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener { status ->
                if (status == PayApiAvailabilityStatus.AVAILABLE) _canSavePasses.value = true
                // } else {
                // We recommend to either:
                // 1) Hide the save button
                // 2) Fall back to a different Save Passes integration (e.g. JWT link)
                // A user might become eligible in the future.
            }
            .addOnFailureListener {
                // Google Play Services is too old. API availability can't be verified.
                _canUseGooglePay.value = false
            }
    }

    /**
     * Creates an object of the pre-created class to save using the API.
     * TODO: Replace with the backend
     */
    fun generateGenericClassObject(): String {
        return """
            {
              "iss": "generic-pass-test@gpay-loyaltyapi-codelab.iam.gserviceaccount.com",
              "aud": "google",
              "typ": "savetoandroidpay",
              "iat": 1368029586,
              "payload": {
                "genericObjects": [
                    {
                      "id" : "$passesIssuerId.generic_pass_sample_object"
                    }
                ]
              },
              "origins": ["http://baconrista.com", "https://baconrista.com"]
            }
        """.trimIndent()
    }
}