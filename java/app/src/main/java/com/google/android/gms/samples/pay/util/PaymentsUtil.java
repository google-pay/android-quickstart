/*
 * Copyright 2024 Google Inc.
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

package com.google.android.gms.samples.pay.util;

import static com.google.android.gms.samples.pay.Constants.TAX_RATE;

import android.content.Context;
import com.google.android.gms.samples.pay.Constants;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import java.math.BigDecimal;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * <p>Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
public class PaymentsUtil {

  /**
   * newShippingOptionParams - Encapsulated shipping option parameters (set of options) definition
   */
  public static JSONObject getShippingOptionParameters() throws JSONException {
    JSONObject shippingOptionParameters = new JSONObject();
    JSONArray shippingOptions = new JSONArray();
    shippingOptions.put(
        createShippingOption(
            "shipping-001", "$0.00: Free shipping label", "Free Shipping example text"));
    shippingOptions.put(
        createShippingOption(
            "shipping-002", "$1.99: Standard shipping label", "Standard shipping example text."));
    shippingOptions.put(
        createShippingOption(
            "shipping-003", "$1000: Express shipping label", "Express shipping example text."));
    shippingOptions.put(
        createShippingOption(
            "shipping-004", "$2000: Same-day shipping label", "Same-day shipping example text."));
    shippingOptionParameters.put("shippingOptions", shippingOptions);
    // set a default shipping option
    shippingOptionParameters.put("defaultSelectedOptionId", "shipping-001");
    return shippingOptionParameters;
  }

  /** createShippingOption - Defines an encapsulated shipping option */
  private static JSONObject createShippingOption(String id, String label, String description)
      throws JSONException {
    return new JSONObject()
        .put("id", id)
        .put("label", label)
        .put("description", description);
  }

  /** createDisplayItem - Encapsulated definition for a display item */
  public static JSONObject createDisplayItem(String label, String type, String price)
      throws JSONException {
    return new JSONObject().put("label", label).put("type", type).put("price", price);
  }

  /**
   * Create a Google Pay API base request object with properties used in all requests.
   *
   * @return Google Pay API base request object.
   * @throws JSONException if the object is malformed.
   */
  private static JSONObject getBaseRequest() throws JSONException {
    return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
  }

  /**
   * Creates an instance of {@link PaymentsClient} for use in an {@link Context} using the
   * environment and theme set in {@link Constants}.
   *
   * @param context is the caller's context.
   */
  public static PaymentsClient createPaymentsClient(Context context) {
    Wallet.WalletOptions walletOptions =
        new Wallet.WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
            .build();
    return Wallet.getPaymentsClient(context, walletOptions);
  }

  /**
   * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
   *
   * <p>The Google Pay API response will return an encrypted payment method capable of being charged
   * by a supported gateway after payer authorization.
   *
   * <p>TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
   *
   * @return Payment data tokenization for the CARD payment method.
   * @throws JSONException if the object is malformed.
   * @see <a href=
   *     "https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
   */
  private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
    return new JSONObject()
        .put("type", "PAYMENT_GATEWAY")
        .put(
            "parameters",
            new JSONObject()
                .put("gateway", "example")
                .put("gatewayMerchantId", "exampleGatewayMerchantId"));
  }

  /**
   * {@code DIRECT} Integration: Decrypt a response directly on your servers. This configuration has
   * additional data security requirements from Google and additional PCI DSS compliance complexity.
   *
   * <p>Please refer to the documentation for more information about {@code DIRECT} integration. The
   * type of integration you use depends on your payment processor.
   *
   * @return Payment data tokenization for the CARD payment method.
   * @throws JSONException if the object is malformed.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
   */
  private static JSONObject getDirectTokenizationSpecification()
      throws JSONException, RuntimeException {
    return new JSONObject()
        .put("type", "DIRECT")
        .put("parameters", new JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS));
  }

  /**
   * Card networks supported by your app and your gateway.
   *
   * <p>TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
   *
   * @return Allowed card networks
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private static JSONArray getAllowedCardNetworks() {
    return new JSONArray(Constants.SUPPORTED_NETWORKS);
  }

  /**
   * Card authentication methods supported by your app and your gateway.
   *
   * <p>TODO: Confirm your processor supports Android device tokens on your supported card networks
   * and make updates in Constants.java.
   *
   * @return Allowed card authentication methods.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private static JSONArray getAllowedCardAuthMethods() {
    return new JSONArray(Constants.SUPPORTED_METHODS);
  }

  /**
   * Describe your app's support for the CARD payment method.
   *
   * <p>The provided properties are applicable to both an IsReadyToPayRequest and a
   * PaymentDataRequest.
   *
   * @return A CARD PaymentMethod object describing accepted cards.
   * @throws JSONException if the object is malformed.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private static JSONObject getBaseCardPaymentMethod() throws JSONException {
    return new JSONObject()
        .put("type", "CARD")
        .put(
            "parameters",
            new JSONObject()
                .put("allowedAuthMethods", getAllowedCardAuthMethods())
                .put("allowedCardNetworks", getAllowedCardNetworks())
                .put("billingAddressRequired", true)
                .put("billingAddressParameters", new JSONObject().put("format", "FULL")));
  }

  /**
   * Describe the expected returned payment data for the CARD payment method
   *
   * @return A CARD PaymentMethod describing accepted cards and optional fields.
   * @throws JSONException if the object is malformed.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private static JSONObject getCardPaymentMethod() throws JSONException {
    return getBaseCardPaymentMethod()
        .put("tokenizationSpecification", getGatewayTokenizationSpecification());
  }

  /**
   * Return a collection of payment methods allowed to complete the operation with Google Pay.
   *
   * @return A JSONArray object with the list of payment methods.
   * @throws JSONException if the JSON object is malformed.
   */
  public static JSONArray getAllowedPaymentMethods() throws JSONException {
    return new JSONArray().put(getCardPaymentMethod());
  }

  /**
   * An object describing accepted forms of payment by your app, used to determine a viewer's
   * readiness to pay.
   *
   * @return API version and payment methods supported by the app.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#IsReadyToPayRequest">IsReadyToPayRequest</a>
   */
  public static JSONObject getIsReadyToPayRequest() throws JSONException {
    return getBaseRequest()
        .put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status.
   *
   * @return information about the requested payment.
   * @throws JSONException if the object is malformed.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
   */
  public static JSONObject getTransactionInfo(String price) throws JSONException {
    return new JSONObject()
        .put("totalPrice", price)
        .put("totalPriceLabel", "Total")
        .put("totalPriceStatus", "FINAL")
        .put("countryCode", Constants.COUNTRY_CODE)
        .put("currencyCode", Constants.CURRENCY_CODE)
        .put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE")
        .put("displayItems", getDisplayItems(price));
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status.
   *
   * @return information about the requested payment.
   * @throws JSONException if the object is malformed.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
   */
  public static JSONArray getDisplayItems(String price) throws JSONException {
    JSONArray displayItems = new JSONArray();
    String Tax =
        String.format(
            Locale.getDefault(), "%.2f", new BigDecimal(price).multiply(new BigDecimal(TAX_RATE)));
    displayItems.put(PaymentsUtil.createDisplayItem("Total", "SUBTOTAL", price));
    displayItems.put(PaymentsUtil.createDisplayItem("Tax", "TAX", Tax));
    return displayItems;
  }

  /**
   * An object describing information to be requested via the Google Pay payment sheet
   *
   * @param priceLabel the price of the product
   * @return Payment data expected by your app.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
   */
  public static JSONObject getPaymentDataRequest(String priceLabel) throws JSONException {
    return PaymentsUtil.getBaseRequest()
        .put("allowedPaymentMethods", getAllowedPaymentMethods())
        .put("transactionInfo", getTransactionInfo(priceLabel))
        .put("merchantInfo", new JSONObject().put("merchantName", Constants.MERCHANT_NAME))
        .put("shippingAddressRequired", true)
        .put("shippingOptionRequired", true)
        .put("shippingOptionParameters", getShippingOptionParameters())
        .put(
            "shippingAddressParameters",
            new JSONObject()
                .put("phoneNumberRequired", false)
                .put("allowedCountryCodes", new JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES)))
        .put(
            "callbackIntents",
            new JSONArray()
                .put("PAYMENT_AUTHORIZATION")
                .put("SHIPPING_ADDRESS")
                .put("SHIPPING_OPTION"));
  }

  /**
   * An object describing information to be updated via the Google Pay payment sheet
   *
   * @param intermediatePaymentData the intermediate payment data containing user selections.
   * @param priceLabel the price of the product.
   * @return Payment data expected by your app.
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
   */
  public static JSONObject getPaymentDataRequestUpdate(
      JSONObject intermediatePaymentData, String priceLabel) throws JSONException {
    // Populate the payment request with default data
    JSONObject paymentDataRequestUpdate = new JSONObject();
    paymentDataRequestUpdate.put("newTransactionInfo", getTransactionInfo(priceLabel));

    JSONObject shippingOptionParameters = getShippingOptionParameters();
    paymentDataRequestUpdate.put("newShippingOptionParameters", shippingOptionParameters);

    // Update the selected shippingOption based on the user selection
    String shippingOptionId = "shipping-001";
    if (intermediatePaymentData.has("shippingOptionData")
        && intermediatePaymentData.getJSONObject("shippingOptionData").has("id")) {
      shippingOptionId =
          intermediatePaymentData.getJSONObject("shippingOptionData").getString("id");
      paymentDataRequestUpdate
          .getJSONObject("newShippingOptionParameters")
          .put("defaultSelectedOptionId", shippingOptionId);
    }
    // Get display item for the selected shipping method and add it to paymentDataRequestUpdate
    JSONObject shippingDisplayItem = getShippingDisplayItem(shippingOptionId);
    paymentDataRequestUpdate
        .getJSONObject("newTransactionInfo")
        .getJSONArray("displayItems")
        .put(shippingDisplayItem); // and data display item
    // define shipping price
    if (shippingDisplayItem.has("price")) {
      // Update displayItems with the new price.
      String totalPrice =
          paymentDataRequestUpdate.getJSONObject("newTransactionInfo").getString("totalPrice");
      String shippingPrice = shippingDisplayItem.getString("price");
      BigDecimal newTotalPriceValue = new BigDecimal(totalPrice).add(new BigDecimal(shippingPrice));
      paymentDataRequestUpdate
          .getJSONObject("newTransactionInfo")
          .put("totalPrice", String.format(Locale.getDefault(), "%.2f", newTotalPriceValue));
    }
    return paymentDataRequestUpdate;
  }

  private static JSONObject getShippingDisplayItem(String shippingOptionId) throws JSONException {

    if (shippingOptionId == null) {
      return new JSONObject();
    }

    // example of how to provide different shipping data depending on user-selected option
    switch (shippingOptionId) {
      case "shipping-001":
        return PaymentsUtil.createDisplayItem("Shipping", "LINE_ITEM", "0");
      case "shipping-002":
        return PaymentsUtil.createDisplayItem("Shipping", "LINE_ITEM", "1.99");
      case "shipping-003":
        return PaymentsUtil.createDisplayItem("Shipping", "LINE_ITEM", "1000");
      case "shipping-004":
        return PaymentsUtil.createDisplayItem("Shipping", "LINE_ITEM", "2000");
      case "shipping_option_unselected":
        return new JSONObject();
      default:
        throw new JSONException("This shipping option is invalid for the given address");
        // example of how to handle an unspecified or invalid shipping option
    }
  }
}
