/*
 * Copyright 2017 Google Inc.
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends Activity {
  /**
   * A client for interacting with the Google Pay API.
   *
   * @see <a
   *     href="https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient">PaymentsClient</a>
   */
  private PaymentsClient mPaymentsClient;

  /**
   * A Google Pay payment button presented to the viewer for interaction.
   *
   * @see <a href="https://developers.google.com/pay/api/android/guides/brand-guidelines">Google Pay
   *     payment button brand guidelines</a>
   */
  private View mGooglePayButton;

  /**
   * Arbitrarily-picked constant integer you define to track a request for payment data activity.
   *
   * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
   */
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

  private TextView mGooglePayStatusText;

  private ItemInfo mBikeItem = new ItemInfo("Simple Bike", 300 * 1000000, R.drawable.bike);
  private long mShippingCost = 90 * 1000000;
  /**
   * Initialize the Google Pay API on creation of the activity
   *
   * @see Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_checkout);

    // Set up the mock information for our item in the UI.
    initItemUI();

    mGooglePayButton = findViewById(R.id.googlepay_button);
    mGooglePayStatusText = findViewById(R.id.googlepay_status);

    // Initialize a Google Pay API client for an environment suitable for testing.
    // It's recommended to create the PaymentsClient object inside of the onCreate method.
    mPaymentsClient = PaymentsUtil.createPaymentsClient(this);
    possiblyShowGooglePayButton();

    mGooglePayButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            requestPayment(view);
          }
        });
  }

  /**
   * Determine the viewer's ability to pay with a payment method supported by your app and display a
   * Google Pay payment button.
   *
   * @see <a href=
   *     "https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html#isReadyToPay(com.google.android.gms.wallet.IsReadyToPayRequest)">PaymentsClient#IsReadyToPay</a>
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
    Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
    task.addOnCompleteListener(
        new OnCompleteListener<Boolean>() {
          @Override
          public void onComplete(Task<Boolean> task) {
            try {
              boolean result = task.getResult(ApiException.class);
              setGooglePayAvailable(result);
            } catch (ApiException exception) {
              // Process error
              Log.w("isReadyToPay failed", exception);
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
      mGooglePayStatusText.setVisibility(View.GONE);
      mGooglePayButton.setVisibility(View.VISIBLE);
    } else {
      mGooglePayStatusText.setText(R.string.googlepay_status_unavailable);
    }
  }

  /**
   * Handle a resolved activity from the Google Pay payment sheet.
   *
   * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
   * @param resultCode Result code returned by the Google Pay API.
   * @param data Intent from the Google Pay API containing payment or error data.
   * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
   *     from an Activity</a>
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
        mGooglePayButton.setClickable(true);
        break;
    }
  }

  /**
   * PaymentData response object contains the payment information, as well as any additional
   * requested information, such as billing and shipping address.
   *
   * @param paymentData A response object returned by Google after a payer approves payment.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentData">Payment
   *     Data</a>
   */
  private void handlePaymentSuccess(PaymentData paymentData) {
    String paymentInformation = paymentData.toJson();

    // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
    if (paymentInformation == null) {
      return;
    }
    JSONObject paymentMethodData;

    try {
      paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
      // If the gateway is set to "example", no payment information is returned - instead, the
      // token will only consist of "examplePaymentMethodToken".
      if (paymentMethodData
              .getJSONObject("tokenizationData")
              .getString("type")
              .equals("PAYMENT_GATEWAY")
          && paymentMethodData
              .getJSONObject("tokenizationData")
              .getString("token")
              .equals("examplePaymentMethodToken")) {
        AlertDialog alertDialog =
            new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage(
                    "Gateway name set to \"example\" - please modify "
                        + "Constants.java and replace it with your own gateway.")
                .setPositiveButton("OK", null)
                .create();
        alertDialog.show();
      }

      String billingName =
          paymentMethodData.getJSONObject("info").getJSONObject("billingAddress").getString("name");
      Log.d("BillingName", billingName);
      Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG)
          .show();

      // Logging token string.
      Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"));
    } catch (JSONException e) {
      Log.e("handlePaymentSuccess", "Error: " + e.toString());
      return;
    }
  }

  /**
   * At this stage, the user has already seen a popup informing them an error occurred. Normally,
   * only logging is required.
   *
   * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
   *     WalletConstants.ERROR_CODE_* constants.
   * @see <a
   *     href="https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants#constant-summary">
   *     Wallet Constants Library</a>
   */
  private void handleError(int statusCode) {
    Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
  }

  // This method is called when the Pay with Google button is clicked.
  public void requestPayment(View view) {
    // Disables the button to prevent multiple clicks.
    mGooglePayButton.setClickable(false);

    // The price provided to the API should include taxes and shipping.
    // This price is not displayed to the user.
    String price = PaymentsUtil.microsToString(mBikeItem.getPriceMicros() + mShippingCost);

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
          mPaymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }
  }

  private void initItemUI() {
    TextView itemName = findViewById(R.id.text_item_name);
    ImageView itemImage = findViewById(R.id.image_item_image);
    TextView itemPrice = findViewById(R.id.text_item_price);

    itemName.setText(mBikeItem.getName());
    itemImage.setImageResource(mBikeItem.getImageResourceId());
    itemPrice.setText(PaymentsUtil.microsToString(mBikeItem.getPriceMicros()));
  }
}
