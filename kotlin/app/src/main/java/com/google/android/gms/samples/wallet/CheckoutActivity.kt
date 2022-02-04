/*
 * Copyright 2022 Google Inc.
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

package com.google.android.gms.samples.wallet

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.util.Json
import com.google.android.gms.wallet.*
import kotlinx.android.synthetic.main.activity_checkout.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToLong


/**
 * Checkout implementation for the app
 */
class CheckoutActivity : Activity() {

    /**
     * A client for interacting with the Google Pay API.
     *
     * @see [PaymentsClient](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient)
     */
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var cardRecognitionPendingIntent: PendingIntent
    private val shippingCost = (90 * 1000000).toLong()

    private lateinit var garmentList: JSONArray
    private lateinit var selectedGarment: JSONObject


    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see Activity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Set up the mock information for our item in the UI.
        selectedGarment = fetchRandomGarment()
        displayGarment(selectedGarment)

        // Initialize a Google Pay API client for an environment suitable for testing.
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        possiblyShowGooglePayButton()
        googlePayButton.setOnClickListener { requestPayment() }

        possiblyShowPaymentCardOcrButton()
        paymentCardOcrButton.setOnClickListener { startPaymentCardOcr() }
    }

    /**
     * Determine the viewer's ability to pay with a payment method supported by your app and display a
     * Google Pay payment button.
     *
     * @see [](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html.isReadyToPay
    ) */
    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
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
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val garmentPriceMicros = (selectedGarment.getDouble("price") * 1000000).roundToLong()
        val price = (garmentPriceMicros + shippingCost).microsToString()

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price)
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    /**
     * Calls the
     * {@link PaymentsClient#getPaymentCardRecognitionIntent(PaymentCardRecognitionIntentRequest)}
     * API and fetches the {@link PendingIntent} needed to launch the payment card recognition
     * `Activity`. Sets the "scan card" button to visible if the call is successful.
     */
    private fun possiblyShowPaymentCardOcrButton() {
        // The request can be used to configure the type of the payment card recognition. Currently
        // the only supported type is card OCR, so it is sufficient to call the getDefaultInstance()
        // method.
        val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
        paymentsClient
            .getPaymentCardRecognitionIntent(request)
            .addOnSuccessListener { intentResponse ->
                cardRecognitionPendingIntent = intentResponse.paymentCardRecognitionPendingIntent
                paymentCardOcrButton.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                // The API is not available either because the feature is not enabled on the device
                // or because your app is not registered.
                Log.e(TAG, "Payment card ocr not available.", e)
            }
    }

    /**
     * Starts the payment card recognition `Activity`.
     */
    private fun startPaymentCardOcr() {
        try {
            ActivityCompat.startIntentSenderForResult(
                this@CheckoutActivity,
                cardRecognitionPendingIntent.intentSender,
                PAYMENT_CARD_RECOGNITION_REQUEST_CODE,
                null, 0, 0, 0, null
            )
        } catch (e: SendIntentException) {
            throw RuntimeException("Failed to start payment card recognition.", e)
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet or the payment card recognition
     * {@code Activity}.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    RESULT_CANCELED -> {
                        // Nothing to do here normally - the user simply cancelled without selecting a
                        // payment method.
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                // Re-enables the Google Pay payment button.
                googlePayButton.isClickable = true
            }
            PAYMENT_CARD_RECOGNITION_REQUEST_CODE -> {
                resetPaymentCardRecognitionIntent()
                when (resultCode) {
                    RESULT_OK -> data?.let {
                        PaymentCardRecognitionResult.getFromIntent(intent)
                            ?.let(::handlePaymentCardRecognitionSuccess)
                    }
                    RESULT_CANCELED -> {
                        // The user cancelled the scan card attempt
                    }
                }
            }
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
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")

            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token") == "examplePaymentMethodToken"
            ) {

                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage(
                        "Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway."
                    )
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
            }

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
                "GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
            )

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
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
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    /**
     * Parses the results from the payment card recognition API and displays a toast.
     *
     * @param paymentCardRecognitionResult Result object from the payment card recognition API.
     */
    private fun handlePaymentCardRecognitionSuccess(
        paymentCardRecognitionResult: PaymentCardRecognitionResult
    ) {
        val resultStringBuilder = StringBuilder()
        resultStringBuilder.append("Card recognized. ")
        resultStringBuilder.append("PAN: ").append(paymentCardRecognitionResult.pan).append(" ")
        val creditCardExpirationDate = paymentCardRecognitionResult.creditCardExpirationDate
        if (creditCardExpirationDate != null) {
            resultStringBuilder.append("Expiration date: ")
                .append(creditCardExpirationDate.month)
                .append("/")
                .append(creditCardExpirationDate.year)
        }
        Toast.makeText(this, resultStringBuilder.toString(), Toast.LENGTH_LONG).show()
    }

    /**
     * Resets the Pending intent as it becomes invalid once used.
     */
    private fun resetPaymentCardRecognitionIntent() {
        paymentCardOcrButton.visibility = View.GONE
        possiblyShowPaymentCardOcrButton()
    }

    private fun fetchRandomGarment(): JSONObject {
        if (!::garmentList.isInitialized) {
            garmentList = Json.readFromResources(this, R.raw.tshirts)
        }

        val randomIndex: Int = Math.round(Math.random() * (garmentList.length() - 1)).toInt()
        return garmentList.getJSONObject(randomIndex)
    }

    private fun displayGarment(garment: JSONObject) {
        detailTitle.text = garment.getString("title")
        detailPrice.text = "\$${garment.getString("price")}"

        val escapedHtmlText: String = Html.fromHtml(garment.getString("description")).toString()
        detailDescription.text = Html.fromHtml(escapedHtmlText)

        val imageUri = "@drawable/${garment.getString("image")}"
        val imageResource = resources.getIdentifier(imageUri, null, packageName)
        detailImage.setImageResource(imageResource)
    }

    companion object {
        const val TAG: String = "CheckoutActivity"

        // Arbitrarily-picked constant integer you define to track a request for payment API
        // activities.
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
        const val PAYMENT_CARD_RECOGNITION_REQUEST_CODE = 992
    }
}
