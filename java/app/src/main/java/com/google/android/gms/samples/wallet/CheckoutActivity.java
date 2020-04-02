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

package com.google.android.gms.samples.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.samples.wallet.util.Json;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends Activity {

  // A client for interacting with the Google Pay API.
  private PaymentsClient paymentsClient;

  // Arbitrarily-picked constant integer you define to track a request for payment data activity.
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

  private long shippingCostMicros = 90 * 1000000;

  // UI elements
  private TextView detailTitle;
  private TextView detailPrice;
  private TextView detailDescription;
  private ImageView detailImage;

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

    setContentView(R.layout.activity_checkout);
    initializeUi();

    // Set up the mock information for our item in the UI.
    try {
      selectedGarment = fetchRandomGarment();
      displayGarment(selectedGarment);
    } catch (JSONException e) {
      throw new RuntimeException("The list of garments cannot be loaded");
    }

    // Initialize a Google Pay API client for an environment suitable for testing.
    // It's recommended to create the PaymentsClient object inside of the onCreate method.
    paymentsClient = PaymentsUtil.createPaymentsClient(this);
    possiblyShowGooglePayButton();
  }

  private void initializeUi() {

    googlePayButton = findViewById(R.id.googlePayButton);

    detailTitle = findViewById(R.id.detailTitle);
    detailPrice = findViewById(R.id.detailPrice);
    detailDescription = findViewById(R.id.detailDescription);
    detailImage = findViewById(R.id.detailImage);

    findViewById(R.id.googlePayButton).setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                requestPayment(view);
              }
            });
  }

  private void displayGarment(JSONObject garment) throws JSONException {
    detailTitle.setText(garment.getString("title"));
    detailPrice.setText(String.format("$%.2f", garment.getDouble("price")));

    final String escapedHtmlText = Html.fromHtml(garment.getString("description")).toString();
    detailDescription.setText(Html.fromHtml(escapedHtmlText));

    final String imageUri = String.format("@drawable/%s", garment.getString("image"));
    final int imageResource = getResources().getIdentifier(imageUri, null, getPackageName());
    detailImage.setImageResource(imageResource);
  }

  /**
   * Determine the viewer's ability to pay with a payment method supported by your app and display a
   * Google Pay payment button.
   *
   * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
   *      PaymentsClient.html#isReadyToPay(com.google.android.gms.wallet.
   *      IsReadyToPayRequest)">PaymentsClient#IsReadyToPay</a>
   */
  private void possiblyShowGooglePayButton() {
    final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
    if (!isReadyToPayJson.isPresent()) {
      return;
    }
    IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
    if (request == null) {
      return;
    }

    // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
    // OnCompleteListener to be triggered when the result of the call is known.
    Task<Boolean> task = paymentsClient.isReadyToPay(request);
    task.addOnCompleteListener(this,
        new OnCompleteListener<Boolean>() {
          @Override
          public void onComplete(@NonNull Task<Boolean> task) {
            if (task.isSuccessful()) {
              setGooglePayAvailable(task.getResult());
            } else {
              Log.w("isReadyToPay failed", task.getException());
            }
          }
        });
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
    if (available) {
      googlePayButton.setVisibility(View.VISIBLE);
    } else {
      Toast.makeText(
              this,
              "Unfortunately, Google Pay is not available on this device",
              Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Handle a resolved activity from the Google Pay payment sheet.
   *
   * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
   * @param resultCode Result code returned by the Google Pay API.
   * @param data Intent from the Google Pay API containing payment or error data.
   * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
   *      from an Activity</a>
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        // value passed in AutoResolveHelper
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {

          case Activity.RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(paymentData);
            break;

          case Activity.RESULT_CANCELED:
            // Nothing to here normally - the user simply cancelled without selecting a
            // payment method.
            break;

          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            handleError(status.getStatusCode());
            break;

          default:
            // Do nothing.
        }

        // Re-enables the Google Pay payment button.
        googlePayButton.setClickable(true);
        break;
    }
  }

  /**
   * PaymentData response object contains the payment information, as well as any additional
   * requested information, such as billing and shipping address.
   *
   * @param paymentData A response object returned by Google after a payer approves payment.
   * @see <a href="https://developers.google.com/pay/api/android/reference/
   *      object#PaymentData">PaymentData</a>
   */
  private void handlePaymentSuccess(PaymentData paymentData) {

    // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
    final String paymentInfo = paymentData.toJson();
    if (paymentInfo == null) return;

    try {
      JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
      // If the gateway is set to "example", no payment information is returned - instead, the
      // token will only consist of "examplePaymentMethodToken".

      final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
      final String tokenizationType = tokenizationData.getString("type");
      final String token = tokenizationData.getString("token");

      if ("PAYMENT_GATEWAY".equals(tokenizationType) && "examplePaymentMethodToken".equals(token)) {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage(getString(R.string.gateway_replace_name_example))
                .setPositiveButton("OK", null)
                .create()
                .show();
      }

      final JSONObject info = paymentMethodData.getJSONObject("info");
      final String billingName = info.getJSONObject("billingAddress").getString("name");
      Toast.makeText(
              this, getString(R.string.payments_show_name, billingName),
              Toast.LENGTH_LONG).show();

      // Logging token string.
      Log.d("Google Pay token: ", token);

    } catch (JSONException e) {
      throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
    }
  }

  /**
   * At this stage, the user has already seen a popup informing them an error occurred. Normally,
   * only logging is required.
   *
   * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
   *                   WalletConstants.ERROR_CODE_* constants.
   * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
   *      WalletConstants#constant-summary">Wallet Constants Library</a>
   */
  private void handleError(int statusCode) {
    Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
  }

  public void requestPayment(View view) {

    // Disables the button to prevent multiple clicks.
    googlePayButton.setClickable(false);

    // The price provided to the API should include taxes and shipping.
    // This price is not displayed to the user.
    try {
      long garmentPriceMicros = Math.round(selectedGarment.getDouble("price") * 1000000);
      final String price = PaymentsUtil.microsToString(garmentPriceMicros + shippingCostMicros);

      // TransactionInfo transaction = PaymentsUtil.createTransaction(price);
      Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price);
      if (!paymentDataRequestJson.isPresent()) {
        return;
      }
      PaymentDataRequest request =
              PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

      // Since loadPaymentData may show the UI asking the user to select a payment method, we use
      // AutoResolveHelper to wait for the user interacting with it. Once completed,
      // onActivityResult will be called with the result.
      if (request != null) {
        AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request),
                this, LOAD_PAYMENT_DATA_REQUEST_CODE);
      }

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
