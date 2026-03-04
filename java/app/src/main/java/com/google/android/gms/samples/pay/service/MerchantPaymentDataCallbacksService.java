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

package com.google.android.gms.samples.pay.service;

import androidx.annotation.NonNull;
import com.google.android.gms.samples.pay.activity.MerchantPaymentDataCallbacks;
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks;
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacksService;

/**
 * Service class which hosts the payment data callbacks initiated by Google Play services within a
 * {@link
 * com.google.android.gms.wallet.PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest)}.
 *
 * <p>The callbacks are implemented through MerchantPaymentDataCallbacks returned from {@link
 * #createPaymentDataCallbacks()}.
 */
public class MerchantPaymentDataCallbacksService extends BasePaymentDataCallbacksService {

  @NonNull
  @Override
  protected BasePaymentDataCallbacks createPaymentDataCallbacks() {
    return new MerchantPaymentDataCallbacks();
  }
}
