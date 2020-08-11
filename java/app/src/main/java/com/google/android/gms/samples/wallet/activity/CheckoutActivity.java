/*
 * Copyright 2020 Google Inc.
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

package com.google.android.gms.samples.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutBinding;
import com.google.android.gms.samples.wallet.util.PaymentsUtil;
import com.google.android.gms.samples.wallet.R;
import com.google.android.gms.samples.wallet.util.Json;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import java.util.Locale;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends AppCompatActivity {

  // Arbitrarily-picked constant integer you define to track a request for payment data activity.
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

  private static final long SHIPPING_COST_CENTS = 90 * PaymentsUtil.CENTS_IN_A_UNIT.longValue();

  // A client for interacting with the Google Pay API.
  private PaymentsClient paymentsClient;

  private ActivityCheckoutBinding layoutBinding;
  private View googlePayButton;

  private JSONArray garmentList;
  private JSONObject selectedGarment;

  /**
   * Initialize the Google Pay API on creation of the activity
   *
   * @see Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initializeUi();

    // Set up the mock information for our item in the UI.
    try {
      selectedGarment = fetchRandomGarment();
      displayGarment(selectedGarment);
    } catch (JSONException e) {
      throw new RuntimeException("The list of garments cannot be loaded");
    }

    // 1. Initialize a Google Pay API client specifying the environment to operate on.

    // 2. Implement the method that determines whether or not to show the Google Pay button
    // possiblyShowGooglePayButton()
  }

  /**
   * Handle a resolved activity from the Google Pay payment sheet.
   *
   * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
   * @param resultCode  Result code returned by the Google Pay API.
   * @param data        Intent from the Google Pay API containing payment or error data.
   * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
   * from an Activity</a>
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      // value passed in AutoResolveHelper
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {

          case Activity.RESULT_OK:
            // 7. Process the result of the transaction upon successful completion.
            break;

          case Activity.RESULT_CANCELED:
            // The user cancelled the payment attempt
            break;

          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            handleError(status.getStatusCode());
            break;
        }

        // Re-enables the Google Pay payment button.
        googlePayButton.setClickable(true);
    }
  }

  private void initializeUi() {

    // Use view binding to access the UI elements
    layoutBinding = ActivityCheckoutBinding.inflate(getLayoutInflater());
    setContentView(layoutBinding.getRoot());

    // The Google Pay button is a layout file – take the root view
    googlePayButton = layoutBinding.googlePayButton.getRoot();
    googlePayButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            requestPayment(view);
          }
        });
  }

  private void displayGarment(JSONObject garment) throws JSONException {
    layoutBinding.detailTitle.setText(garment.getString("title"));
    layoutBinding.detailPrice.setText(
        String.format(Locale.getDefault(), "$%.2f", garment.getDouble("price")));

    final String escapedHtmlText = Html.fromHtml(
        garment.getString("description"), Html.FROM_HTML_MODE_COMPACT).toString();
    layoutBinding.detailDescription.setText(Html.fromHtml(
        escapedHtmlText, Html.FROM_HTML_MODE_COMPACT));

    final String imageUri = String.format("@drawable/%s", garment.getString("image"));
    final int imageResource = getResources().getIdentifier(imageUri, null, getPackageName());
    layoutBinding.detailImage.setImageResource(imageResource);
  }

  /**
   * Determine the viewer's ability to pay with a payment method supported by your app and display a
   * Google Pay payment button.
   *
   * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
   * PaymentsClient.html#isReadyToPay(com.google.android.gms.wallet.
   * IsReadyToPayRequest)">PaymentsClient#IsReadyToPay</a>
   */
  private void possiblyShowGooglePayButton() {

    // 3. Populate the fields under the readyToPay request before calling isReadyToPay
    // val isReadyToPayJson = ...

    // 4. Use the previously created object to call isReadyToPay and capture the result
    // val task = paymentsClient.isReadyToPay(request)
    // task.addOnCompleteListener...: setGooglePayAvailable(available)
  }

  /**
   * If isReadyToPay returned {@code true}, show the button and hide the "checking" text. Otherwise,
   * notify the user that Google Pay is not available. Please adjust to fit in with your current
   * user flow. You are not required to explicitly let the user know if isReadyToPay returns {@code
   * false}.
   *
   * @param available isReadyToPay API response.
   */
  private void setGooglePayAvailable(boolean available) {
    // 5. Show the Google Pay button if the user is able to pay using Google Pay.
    // Otherwise, update your UI and show notifications accordingly.
  }

  /**
   * PaymentData response object contains the payment information, as well as any additional
   * requested information, such as billing and shipping address.
   *
   * @param paymentData A response object returned by Google after a payer approves payment.
   * @see <a href="https://developers.google.com/pay/api/android/reference/
   * object#PaymentData">PaymentData</a>
   */
  private void handlePaymentSuccess(PaymentData paymentData) {
    // 8. Inspect the resulting payload and update your application accordingly.
    // For example, you may want to notify the user about the result of the transaction.
    // val paymentInformation = paymentData.toJson() ?: return
  }

  /**
   * At this stage, the user has already seen a popup informing them an error occurred. Normally,
   * only logging is required.
   *
   * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
   *                   WalletConstants.ERROR_CODE_* constants.
   * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
   * WalletConstants#constant-summary">Wallet Constants Library</a>
   */
  private void handleError(int statusCode) {
    Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
  }

  public void requestPayment(View view) {

    // Disables the button to prevent multiple clicks.
    googlePayButton.setClickable(false);

    // The price provided to the API should include taxes and shipping.
    // This price is not displayed to the user.
    try {
      double garmentPrice = selectedGarment.getDouble("price");
      long garmentPriceCents = Math.round(garmentPrice * PaymentsUtil.CENTS_IN_A_UNIT.longValue());
      long priceCents = garmentPriceCents + SHIPPING_COST_CENTS;

      // 6. Construct the payment data request object and initiate the payment transaction
      // by calling loadPaymentData.

    } catch (JSONException e) {
      throw new RuntimeException("The price cannot be deserialized from the JSON object.");
    }
  }

  private JSONObject fetchRandomGarment() {

    // Only load the list of items if it has not been loaded before
    if (garmentList == null) {
      garmentList = Json.readFromResources(this, R.raw.tshirts);
    }

    // Take a random element from the list
    int randomIndex = Math.toIntExact(Math.round(Math.random() * (garmentList.length() - 1)));
    try {
      return garmentList.getJSONObject(randomIndex);
    } catch (JSONException e) {
      throw new RuntimeException("The index specified is out of bounds.");
    }
  }
}
