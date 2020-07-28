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

package com.google.android.gms.samples.wallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.samples.wallet.util.Notifications;

import androidx.annotation.Nullable;

public class PaymentNotificationIntentService extends IntentService {

  public PaymentNotificationIntentService() {
    super("PaymentNotificationIntentService");
  }

  @Override
  protected void onHandleIntent(@Nullable final Intent intent) {

    final String intentAction = intent.getAction();
    if (intentAction.startsWith(Notifications.ACTION_SELECT_PREFIX)) {

      Handler priceHandler = new Handler(Looper.getMainLooper());
      priceHandler.post(new Runnable() {
        @Override
        public void run() {
          final int prefixLength = Notifications.ACTION_SELECT_PREFIX.length();
          final String option = intentAction.substring(prefixLength);
          Notifications.triggerPaymentNotification(getBaseContext(), option);
        }
      });
    }
  }
}
