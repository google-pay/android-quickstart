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

package com.google.android.gms.samples.wallet.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import com.google.android.gms.samples.wallet.R;
import com.google.android.gms.samples.wallet.activity.PaymentTransparentActivity;
import com.google.android.gms.samples.wallet.service.PaymentNotificationIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications {

  private static final int NOTIFICATION_ID = 1;
  private static final String NOTIFICATION_CHANNEL_ID = "payments_channel";

  private static final int REQUEST_CODE_SELECT_OPTION = 239357;
  private static final int REQUEST_CODE_START_ACTIVITY = 27487;

  private static final String OPTION_1 = "option1";
  private static final String OPTION_2 = "option2";
  private static final String OPTION_3 = "option3";

  public static final String ACTION_SELECT_PREFIX = "com.google.android.gms.samples.wallet.SELECT_PRICE";
  public static final String ACTION_PAY_GOOGLE_PAY = "com.google.android.gms.samples.wallet.PAY_GOOGLE_PAY";
  public static final String ACTION_PAY_OTHER = "com.google.android.gms.samples.wallet.PAY_OTHER";

  public static final String OPTION_PRICE_EXTRA = "optionPriceExtra";

  private static final HashMap<String, String> OPTION_BUTTONS = new HashMap<String, String>() {{
    put(OPTION_1, "buttonOption1");
    put(OPTION_2, "buttonOption2");
    put(OPTION_3, "buttonOption3");
  }};

  private static final HashMap<String, Long> OPTION_PRICE_CENTS = new HashMap<String, Long>() {{
    put(OPTION_1, 1000L);
    put(OPTION_2, 2500L);
    put(OPTION_3, 5000L);
  }};

  public static void triggerPaymentNotification(Context context) {
    triggerPaymentNotification(context, OPTION_2);
  }

  /**
   * Triggers/updates a payment notification
   */
  public static void triggerPaymentNotification(Context context, String selectedOption) {

    final Resources res = context.getResources();
    final String packageName = context.getPackageName();

    // Create a custom notification layout
    RemoteViews notificationLayout = new RemoteViews(packageName, R.layout.notification_top_up_account);

    // Creates the selectable options
    final List<String> options = new ArrayList<>(OPTION_PRICE_CENTS.keySet());
    for (String option : options) {

      // Adjust color based on selected option
      int optionColor = res.getColor(R.color.price_button_grey, context.getTheme());
      int optionBg = R.drawable.price_button_background;
      if (option.equals(selectedOption)) {
        optionColor = Color.WHITE;
        optionBg = R.drawable.price_button_background_selected;
      }

      int buttonId = res.getIdentifier(OPTION_BUTTONS.get(option), "id", packageName);
      notificationLayout.setTextColor(buttonId, optionColor);
      notificationLayout.setInt(buttonId, "setBackgroundResource", optionBg);

      // Create pendingIntent to respond to the click event
      Intent selectOptionIntent = new Intent(context, PaymentNotificationIntentService.class);
      selectOptionIntent.setAction(ACTION_SELECT_PREFIX + option);
      notificationLayout.setOnClickPendingIntent(buttonId, PendingIntent.getService(
          context, REQUEST_CODE_SELECT_OPTION, selectOptionIntent,
          PendingIntent.FLAG_UPDATE_CURRENT));
    }

    // Set Google Pay button action
    Intent payIntent = new Intent(context, PaymentTransparentActivity.class);
    payIntent.setAction(ACTION_PAY_GOOGLE_PAY);
    payIntent.putExtra(OPTION_PRICE_EXTRA, OPTION_PRICE_CENTS.get(selectedOption));
    notificationLayout.setOnClickPendingIntent(
        R.id.googlePayButton, pendingIntentForActivity(context, payIntent));

    // Create a notification and set the notification channel
    Notification notification = new NotificationCompat
        .Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_text))
        .setCustomBigContentView(notificationLayout)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(false)
        .setOnlyAlertOnce(true)
        .build();

    // Trigger or update the notification.
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
  }

  public static void remove(Context context) {
    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
  }

  /**
   * Create the notification channel for API 26+, to help the system group notifications
   * based on logical groups that users can make sense of and manage collectively.
   *
   * @param context where the channel is being created from
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
  public static void createNotificationChannelIfNotCreated(Context context) {
    CharSequence name = context.getString(R.string.channel_name);
    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
        name, NotificationManager.IMPORTANCE_HIGH);

    NotificationManager notificationMgr = context.getSystemService(NotificationManager.class);
    notificationMgr.createNotificationChannel(channel);
  }

  private static PendingIntent pendingIntentForActivity(Context context, Intent intent) {
    return PendingIntent.getActivity(
        context, REQUEST_CODE_START_ACTIVITY, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
  }
}