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

package com.google.android.gms.samples.pay.activity;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.samples.pay.Constants;
import com.google.android.gms.samples.pay.util.PaymentsUtil;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks;
import com.google.android.gms.wallet.callback.IntermediatePaymentData;
import com.google.android.gms.wallet.callback.OnCompleteListener;
import com.google.android.gms.wallet.callback.PaymentAuthorizationResult;
import com.google.android.gms.wallet.callback.PaymentDataRequestUpdate;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of {@link BasePaymentDataCallbacks} that handles callbacks from the Google Pay
 * payment sheet.
 */
public class MerchantPaymentDataCallbacks extends BasePaymentDataCallbacks {

  /**
   * Handles payment data changes in the payment sheet such as shipping address and shipping
   * options. Values passed back to it will update the payment sheet.
   *
   * @param request The updated payment data from the payment sheet.
   * @param onCompleteListener The listener to call when the update is complete.
   */
  @Override
  public void onPaymentDataChanged(
      IntermediatePaymentData request,
      @NonNull OnCompleteListener<PaymentDataRequestUpdate> onCompleteListener) {
    // define prices and variables
    JSONObject paymentDataRequestUpdate;
    Bundle newSavedState = new Bundle();
    try {
      assert request != null;
      JSONObject intermediatePaymentData = new JSONObject(request.toJson());
      // define transaction info
      paymentDataRequestUpdate =
          PaymentsUtil.getPaymentDataRequestUpdate(intermediatePaymentData, Constants.BASE_PRICE);
      newSavedState.putString("paymentDataRequestUpdate", paymentDataRequestUpdate.toString());
      // return the generated data to the client
      onCompleteListener.complete(
          PaymentDataRequestUpdate.fromJson(paymentDataRequestUpdate.toString())
              .withUpdatedSavedState(newSavedState));
    } catch (JSONException e) {
      Log.e("SampleMerchantPaymentDataCallbacksService", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Called when a payment is authorized in the payment sheet. Use this callback to perform any
   * final validation on the payment data. Throwing an error will allow the user to make
   * corrections to the payment sheet.
   *
   * @param request The payment data from the payment sheet.
   * @param onCompleteListener The listener to call when the authorization is complete.
   */
  @Override
  public void onPaymentAuthorized(
      PaymentData request,
      @NonNull OnCompleteListener<PaymentAuthorizationResult> onCompleteListener) {
    Log.i("Invocation", "onPaymentAuthorized invoked");
    Bundle savedState = new Bundle();
    JSONObject paymentAuthorizationResultJson = new JSONObject();
    try {
      assert request != null;
      JSONObject paymentDataJson = new JSONObject(request.toJson());
      JSONObject info = paymentDataJson.getJSONObject("paymentMethodData").getJSONObject("info");
      // example of how to check for a card network
      String cardNetwork = info.getString("cardNetwork");
      if (cardNetwork.equals("VISA")) {
        paymentAuthorizationResultJson.put("transactionState", "SUCCESS");
      } else {
        paymentAuthorizationResultJson.put("transactionState", "ERROR");
        JSONObject error =
            new JSONObject()
                .put("reason", "PAYMENT_DATA_INVALID")
                .put("message", "Only Visa is accepted!")
                .put("intent", "PAYMENT_AUTHORIZATION");
        paymentAuthorizationResultJson.put("error", error);
      }
      onCompleteListener.complete(
          PaymentAuthorizationResult.fromJson(paymentAuthorizationResultJson.toString())
              .withUpdatedSavedState(savedState));
    } catch (JSONException e) {
      Log.e("SampleMerchantPaymentDataCallbacksService", Objects.requireNonNull(e.getMessage()));
      throw new RuntimeException(e);
    }
  }
}
