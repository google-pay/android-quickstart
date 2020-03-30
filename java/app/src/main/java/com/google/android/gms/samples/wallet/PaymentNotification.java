package com.google.android.gms.samples.wallet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.RemoteViews;

public class PaymentNotification {

    static int NOTIFICATION_ID = 1;
    static String CHANNEL_ID = "payment_channel_01";// The id of the channel.

    private PaymentNotification() {
    }

    static NotificationManager getManager(Context context){

        CharSequence name = context.getString(R.string.channel_name);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);

        return  mNotificationManager;
    }

    /**
     * Triggers/updates a payment notification
     */
    static public void trigger(Context context, int selectedOption) {

        // creates a custom notification layout (small and large)
        // potentially there is no need for either
        RemoteViews notificationLayoutExpanded = new RemoteViews(context.getPackageName(),
                R.layout.large_notification);

        // creates the intent call
        Intent callIntent = new Intent(context, PaymentNotificationIntentService.class);
        String price = "";
        switch (selectedOption){
            case 1:
                price= "10.00";
                break;
            case 2:
                price= "25.00";
                break;
            case 3:
                price= "50.00";
                break;
        }
        callIntent.putExtra("price", price);

        Resources res = context.getResources();

        // creates the selectable options
        for (int i = 1; i <= 3; i++) {

            // finds the option button
            int buttonId = res.getIdentifier("buttonPrice" + i, "id", context.getPackageName());

            // sets the action
            callIntent.setAction("option" + i);
            PendingIntent clickPI = PendingIntent
                    .getActivity(context, 0,
                            callIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            notificationLayoutExpanded.setTextColor(buttonId, i == selectedOption ? Color.RED : Color.GRAY);
            notificationLayoutExpanded.setOnClickPendingIntent(buttonId,
                    PendingIntent.getService(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        // set GPay button
        callIntent.setAction("gpay");
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.googlepay_button,
                PendingIntent.getService(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        // set other pay button
        callIntent.setAction("pay_other");
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.buttonPayOther,
                PendingIntent.getService(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));


        // creates a notification and set the notification channel.
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build();


        // trigger the notification.
        getManager(context).notify(NOTIFICATION_ID, notification);
    }

    static void remove(Context context){

        // cancel the notification.
        getManager(context).cancel(NOTIFICATION_ID);
    }

}
