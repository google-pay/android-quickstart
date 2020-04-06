package com.google.android.gms.samples.wallet;

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
import com.google.android.gms.samples.wallet.activity.CheckoutActivity;
import com.google.android.gms.samples.wallet.activity.PaymentTransparentActivity;
import com.google.android.gms.samples.wallet.service.PaymentNotificationIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class Notifications {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "payments_channel";

    public static final String ACTION_SELECT_OPTION = "action_select_option";
    public static final String ACTION_PAY_GOOGLE_PAY = "action_pay_google_pay";
    public static final String ACTION_PAY_OTHER = "action_pay_other";

    public static final String SELECTED_OPTION_EXTRA = "selectedOptionExtra";
    public static final String OPTION_PRICE_EXTRA = "optionPriceExtra";

    private static final String OPTION_1 = "option1";
    private static final String OPTION_2 = "option2";
    private static final String OPTION_3 = "option3";

    private static final HashMap<String, String> BUTTON_OPTIONS = new HashMap<String, String>() {{
        put("buttonOption1", OPTION_1);
        put("buttonOption2", OPTION_2);
        put("buttonOption3", OPTION_3);
    }};

    private static final HashMap<String, Long> OPTION_PRICE_CENTS = new HashMap<String, Long>() {{
        put(OPTION_1, 1000L);
        put(OPTION_2, 2500L);
        put(OPTION_3, 5000L);
    }};

    private static NotificationManager getManager(Context context){
        return context.getSystemService(NotificationManager.class);
    }

    public static void triggerPaymentNotification(Context context) {
        triggerPaymentNotification(context, OPTION_2);
    }

    /**
     * Triggers/updates a payment notification
     */
    public static void triggerPaymentNotification(Context context, String selectedOption) {

        // Create a custom notification layout
        RemoteViews notificationLayoutExpanded = new RemoteViews(context.getPackageName(),
                R.layout.large_notification);

        // Creates the selectable options
        final List<String> options = new ArrayList<>(OPTION_PRICE_CENTS.keySet());
        for (String option : options) {

            // Adjust color based on selected option
            final int optionColor = option.equals(selectedOption) ? Color.RED : Color.GRAY;
            int buttonId = context.getResources().getIdentifier(
                    BUTTON_OPTIONS.get(option), "id", context.getOpPackageName());
            notificationLayoutExpanded.setTextColor(buttonId, optionColor);

            // Create pendingIntent to respond to the click event
            Intent selectOptionIntent = new Intent(context, PaymentNotificationIntentService.class);
            selectOptionIntent.setAction(ACTION_SELECT_OPTION);
            selectOptionIntent.putExtra(SELECTED_OPTION_EXTRA, option);
            notificationLayoutExpanded.setOnClickPendingIntent(buttonId,
                    createPendingIntent(context, selectOptionIntent));
        }

        // Set Google Pay button action
        Intent payIntent = new Intent(context, PaymentTransparentActivity.class);
        payIntent.putExtra(OPTION_PRICE_EXTRA, OPTION_PRICE_CENTS.get(selectedOption));
        notificationLayoutExpanded.setOnClickPendingIntent(
                R.id.googlePayButton, pendingIntentForActivity(context, payIntent));

        // Set other pay button
        Intent goBackIntent = new Intent(context, CheckoutActivity.class);
        notificationLayoutExpanded.setOnClickPendingIntent(
                R.id.buttonPayOther, pendingIntentForActivity(context, goBackIntent));

        // Create a notification and set the notification channel
        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setCustomBigContentView(notificationLayoutExpanded)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build();

        // Trigger or update the notification.
        getManager(context).notify(NOTIFICATION_ID, notification);
    }

    public static void remove(Context context){
        getManager(context).cancel(NOTIFICATION_ID);
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

        // Dismiss notification
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        // Create pending intent
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return createPendingIntent(context, intent);
    }

    private static PendingIntent createPendingIntent(Context context, Intent intent) {
        return PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}