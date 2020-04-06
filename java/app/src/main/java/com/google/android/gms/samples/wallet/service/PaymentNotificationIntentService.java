package com.google.android.gms.samples.wallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.samples.wallet.util.Notifications;

import androidx.annotation.Nullable;

public class PaymentNotificationIntentService extends IntentService {

    public PaymentNotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {

        final String intentAction = intent.getAction();
        if(intentAction.startsWith(Notifications.ACTION_SELECT_PREFIX)) {

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
