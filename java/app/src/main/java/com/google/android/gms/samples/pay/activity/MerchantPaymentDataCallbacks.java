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
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks;
import com.google.android.gms.wallet.callback.IntermediatePaymentData;
import com.google.android.gms.wallet.callback.OnCompleteListener;
import com.google.android.gms.wallet.callback.PaymentAuthorizationResult;
import com.google.android.gms.wallet.callback.PaymentDataRequestUpdate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
public class MerchantPaymentDataCallbacks extends BasePaymentDataCallbacks {
    /**
     * newShippingOptionParams - Encapsulated shipping option parameters (set of options)
     * definition
     */
    private static JSONObject newShippingOptionParams(String curShippingOptionId)
            throws JSONException {
        JSONObject shippingOptionParameters = new JSONObject();
        JSONArray shippingOptions = new JSONArray();
        shippingOptions.put(
                createShippingOption(
                        "shipping-001",
                        "$0.00: Free shipping label",
                        "Free Shipping example text"));
        shippingOptions.put(
                createShippingOption(
                        "shipping-002",
                        "$1.99: Standard shipping label",
                        "Standard shipping example text."));
        shippingOptions.put(
                createShippingOption(
                        "shipping-003",
                        "$1000: Express shipping label",
                        "Express shipping example text."));
        shippingOptions.put(
                createShippingOption(
                        "shipping-004",
                        "$2000: Same-day shipping label",
                        "Same-day shipping example text."));
        shippingOptionParameters.put("shippingOptions", shippingOptions);
        String[] shippingOptionIds = {
                "shipping-001", "shipping-002", "shipping-003", "shipping-004"
        };
        // set a default shipping option
        if (new HashSet<>(Arrays.asList(shippingOptionIds)).contains(curShippingOptionId)) {
            shippingOptionParameters.put("defaultSelectedOptionId", curShippingOptionId);
        } else {
            shippingOptionParameters.put("defaultSelectedOptionId", "shipping-001");
        }
        return shippingOptionParameters;
    }
    /**
     * createShippingOption - Defines an encapsulated shipping option
     */
    private static JSONObject createShippingOption(String id, String label, String description)
            throws JSONException {
        return new JSONObject().put("id", id).put("label", label).put("description", description);
    }
    /**
     * createDisplayItem - Encapsulated definition for a display item
     */
    private static JSONObject createDisplayItem(String label, String type, String price)
            throws JSONException {
        return new JSONObject().put("label", label).put("type", type).put("price", price);
    }
    /**
     * onPaymentDataChanged callback - Handles payment data changes in the payment sheet such as
     * shipping address and shipping options.
     */
    @Override
    protected void onPaymentDataChanged(
            IntermediatePaymentData request,
            OnCompleteListener<PaymentDataRequestUpdate> OnCompleteListener) {
        // define prices and variables
        String totalPrice = "2.9";
        String subTotal = "1.0";
        String tax = "0.0";
        String shippingPrice = "0.0";
        JSONObject paymentDataRequestUpdate = new JSONObject();
        Bundle newSavedState = new Bundle();
        try {
            JSONObject intermediatePaymentDataJson = new JSONObject(request.toJson());
            // define transaction info
            JSONObject newTransactionInfo = new JSONObject();
            newTransactionInfo
                    .put("currencyCode", "USD")
                    .put("totalPriceLabel", "FINAL")
                    .put("totalPrice", totalPrice);
            // process user-provided shipping option data
            String shippingOptionId = "shipping-001";
            if (intermediatePaymentDataJson.has("shippingOptionData")
                    && intermediatePaymentDataJson.getJSONObject("shippingOptionData").has("id")) {
                shippingOptionId =
                        intermediatePaymentDataJson.getJSONObject("shippingOptionData").getString("id");
            }
            // define shipping options
            if (intermediatePaymentDataJson.has("shippingAddress")) {
                paymentDataRequestUpdate.put(
                        "newShippingOptionParameters",
                        newShippingOptionParams(shippingOptionId));
                if (paymentDataRequestUpdate.has("newShippingOptionParameters")) {
                    shippingOptionId =
                            paymentDataRequestUpdate
                                    .getJSONObject("newShippingOptionParameters")
                                    .getString("defaultSelectedOptionId");
                }
            }
            // define displayItems
            JSONArray displayItems = new JSONArray();
            displayItems.put(createDisplayItem("Subtotal", "SUBTOTAL", subTotal));
            displayItems.put(createDisplayItem("Estimated tax", "TAX", tax));
            // get shipping data
            JSONObject shippingData = getShippingData(shippingOptionId);
            // define shipping price
            if (shippingData.has("price")) {
                shippingPrice = shippingData.getString("price");
                displayItems.put(shippingData); // and data display item
            }
            // recalculate total price
            double newTotalPriceValue =
                    Double.parseDouble(totalPrice) + Double.parseDouble(shippingPrice);
            newTransactionInfo.put(
                    "totalPrice", String.format(Locale.getDefault(), "%.2f", newTotalPriceValue));
            // save all of the data we generated above into the appropriate objects
            paymentDataRequestUpdate.put("newTransactionInfo", newTransactionInfo);
            newSavedState.putString("paymentDataRequestUpdate", paymentDataRequestUpdate.toString());
            // return the generated data to the client
            OnCompleteListener.complete(
                    PaymentDataRequestUpdate.fromJson(paymentDataRequestUpdate.toString())
                            .withUpdatedSavedState(newSavedState));
        } catch (JSONException e) {
            Log.e("SampleMerchantPaymentDataCallbacksService", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    /**
     * onPaymentAuthorized callback - Called when a payment is authorized in the payment sheet.
     */
    @Override
    protected void onPaymentAuthorized(
            PaymentData request, OnCompleteListener<PaymentAuthorizationResult> OnCompleteListener) {
        Log.i("Invocation", "onPaymentAuthorized invoked");
        Bundle savedState = new Bundle();
        JSONObject paymentAuthorizationResultJson = new JSONObject();
        try {
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
            OnCompleteListener.complete(
                    PaymentAuthorizationResult.fromJson(paymentAuthorizationResultJson.toString())
                            .withUpdatedSavedState(savedState));
        } catch (JSONException e) {
            Log.e("SampleMerchantPaymentDataCallbacksService", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    static JSONObject getShippingData(String shippingOptionId)
            throws JSONException {

        if (shippingOptionId == null) {
            return new JSONObject();
        }

        // example of how to provide different shipping data depending on user-selected option
        switch (shippingOptionId) {
            case "shipping-001":
                return createDisplayItem("Shipping", "LINE_ITEM", "0");
            case "shipping-002":
                return createDisplayItem("Shipping", "LINE_ITEM", "1.99");
            case "shipping-003":
                return createDisplayItem("Shipping", "LINE_ITEM", "1000");
            case "shipping-004":
                return createDisplayItem("Shipping", "LINE_ITEM", "2000");
            case "shipping_option_unselected":
                return new JSONObject();
            default:
                throw new JSONException("This shipping option is invalid for the given address");
                // example of how to handle an unspecified or invalid shipping option
        }
    }
}
