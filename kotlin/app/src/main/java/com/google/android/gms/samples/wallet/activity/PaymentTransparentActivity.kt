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

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.util.Notifications
import com.google.android.gms.samples.wallet.util.PaymentsUtil.createPaymentsClient
import com.google.android.gms.samples.wallet.util.PaymentsUtil.getPaymentDataRequest
import com.google.android.gms.wallet.*
import org.json.JSONException
import org.json.JSONObject


/**
 * Checkout implementation for the app
 */
class PaymentTransparentActivity : AppCompatActivity() {

  // Arbitrarily-picked constant integer you define to track a request for payment data activity.
  private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Dismiss the notification UI if the activity was opened from a notification
    if (Notifications.ACTION_PAY_GOOGLE_PAY == intent.action) {
      sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }

    // Initialise the payments client
    startPayment()
  }

  /**
   * Handle a resolved activity from the Google Pay payment sheet.
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
          Activity.RESULT_OK ->
            data?.let { intent ->
              Notifications.remove(this)
              PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
            }

          Activity.RESULT_CANCELED -> {
            // The user simply cancelled without selecting a payment method.
          }

          AutoResolveHelper.RESULT_ERROR -> {
            // Get more details on the error with – AutoResolveHelper.getStatusFromIntent(data);
          }
        }

        // Close the activity
        finishAndRemoveTask()
      }
    }
  }

  private fun startPayment() {

    // Fetch the price based on the user selection
    val priceCents = intent.getLongExtra(Notifications.OPTION_PRICE_EXTRA, 2500L)

    val paymentDataRequestJson = getPaymentDataRequest(priceCents)
    val request = paymentDataRequestJson.let { PaymentDataRequest.fromJson(it.toString()) } ?: return

    val paymentsClient = createPaymentsClient(this)
    AutoResolveHelper.resolveTask(
        paymentsClient.loadPaymentData(request),
        this, LOAD_PAYMENT_DATA_REQUEST_CODE)
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
    val paymentInformation = paymentData.toJson() ?: return

    try {
      // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
      val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
      val billingName = paymentMethodData.getJSONObject("info")
          .getJSONObject("billingAddress").getString("name")
      Log.d("BillingName", billingName)

      Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show()

      // Logging token string.
      Log.d("GooglePaymentToken", paymentMethodData
          .getJSONObject("tokenizationData")
          .getString("token"))

    } catch (e: JSONException) {
      Log.e("handlePaymentSuccess", "Error: $e")
    }
  }
}
