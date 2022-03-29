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
import java.util.*

import kotlin.random.Random

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    // A client for interacting with the Google Pay API.
    private val paymentsClient: PaymentsClient = PaymentsUtil.createPaymentsClient(application)

    // A client to interact with the Google Wallet API
    private val walletClient: PayClient = Pay.getClient(application)

    // LiveData with the result of whether the user can pay using Google Pay
    private val _canUseGooglePay: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanUseGooglePay()
        }
    }

    // LiveData with the result of whether the user can save passes to Google Wallet
    private val _canSavePasses: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanAddPassesToGoogleWallet()
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
    private fun fetchCanAddPassesToGoogleWallet() {
        walletClient
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener { status ->
                _canSavePasses.value = status == PayApiAvailabilityStatus.AVAILABLE
                // } else {
                // We recommend to either:
                // 1) Hide the save button
                // 2) Fall back to a different Save Passes integration (e.g. JWT link)
                // Note that a user might become eligible in the future.
            }
            .addOnFailureListener {
                // Google Play Services is too old. API availability can't be verified.
                _canUseGooglePay.value = false
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

    // Configuration for the issuer and test pass
    private val issuerId = "3388000000022114540"
    private val passClass = "3388000000022114540.c0898315-2a7e-4dfa-b7ba-9dc5be4f2496"
    private val passId = UUID.randomUUID()
    private val passId = UUID.randomUUID().toString()

    // Test generic object used to be created against the API
    val mockObjectJson = """
        {
          "iss": "generic-pass-test@gpay-loyaltyapi-codelab.iam.gserviceaccount.com",
          "aud": "google",
          "typ": "savetowallet",
          "iat": ${Date().time / 1000L},
          "origins": [],
          "payload": {
            "genericObjects": [
              {
                "id": "$issuerId.$passId",
                "classId": "$passClass",
                "genericType": "GENERIC_TYPE_UNSPECIFIED",
                "hexBackgroundColor": "#4285f4",
                "logo": {
                  "sourceUri": {
                    "uri": "https://storage.googleapis.com/wallet-lab-tools-codelab-artifacts-public/pass_google_logo.jpg"
                  }
                },
                "cardTitle": {
                  "defaultValue": {
                    "language": "en",
                    "value": "Google I/O '22  [DEMO ONLY]"
                  }
                },
                "subheader": {
                  "defaultValue": {
                    "language": "en",
                    "value": "Attendee"
                  }
                },
                "header": {
                  "defaultValue": {
                    "language": "en",
                    "value": "Alex McJacobs"
                  }
                },
                "barcode": {
                  "type": "QR_CODE",
                  "value": "$passId"
                },
                "heroImage": {
                  "sourceUri": {
                    "uri": "https://storage.googleapis.com/wallet-lab-tools-codelab-artifacts-public/google-io-hero-demo-only-2.png"
                  }
                },
                "notifications": {
                  "upcomingNotification": {
                    "enableNotification": true
                  }
                },
                "textModulesData": [
                  {
                    "header": "POINTS",
                    "body": "${Random.nextInt(0, 9999)}",
                    "id": "points"
                  },
                  {
                    "header": "CONTACTS",
                    "body": "${Random.nextInt(1, 99)}",
                    "id": "contacts"
                  }
                ]
              }
            ]
          }
        }
        """
}