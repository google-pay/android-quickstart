package com.google.android.gms.samples.wallet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONObject;

import java.util.Optional;

public class PaymentNotificationIntentService extends IntentService {

    private PaymentsClient mPaymentsClient;

    private Button buttonPrice1, buttonPrice2, buttonPrice3;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PaymentNotificationIntentService() {
        super("notificationIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // trigger IsReadyToPay
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT).build();
        mPaymentsClient = Wallet.getPaymentsClient(getApplicationContext(), walletOptions);

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
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            //setGooglePayAvailable(task.getResult());
                            Log.w("isReadyToPay true", "success");
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        switch (intent.getAction()) {
            case "option1":
            case "option2":
            case "option3":
                Handler priceHandler = new Handler(Looper.getMainLooper());
                priceHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        PaymentNotification.trigger(getBaseContext(), Integer.parseInt(intent.getAction().substring(intent.getAction().length() - 1)));

                    }
                });
                break;
            case "gpay":
                Handler gpayHandler = new Handler(Looper.getMainLooper());
                gpayHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // start a transparent activity to launch the payment sheet
                        Intent i = new Intent();
                        i.setClass(getBaseContext(), PaymentTransparentActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("price", intent.getExtras().getString("price"));
                        startActivity(i);

                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    }
                });
                break;
            case "pay_other":
                Handler otherHandler = new Handler(Looper.getMainLooper());
                otherHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // start a transparent activity to launch the payment sheet
                        Intent i = new Intent();
                        i.setClass(getBaseContext(), CheckoutActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);

                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    }
                });
                break;
        }
    }

    public void triggerNotification() {

        // creates a custom notification layout (small and large)
        // potentially there is no need for either
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.large_notification);

        //notificationLayoutExpanded.s

        Intent callIntent = new Intent(this, PaymentNotificationIntentService.class);
        callIntent.setAction("left");
        PendingIntent clickPI = PendingIntent
                .getActivity(this, 0,
                        callIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayoutExpanded.setTextColor(R.id.buttonPrice1, Color.RED);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.buttonPayOther, PendingIntent.getService(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.buttonPrice1, PendingIntent.getService(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));


        String CHANNEL_ID = "payment_channel_01";// The id of the channel.
        CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        // creates a notification and set the notification channel.
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("New Message")
                .setContentText("You've received new messages.")
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID)
                .build();

        // Sets an ID for the notification, so it can be updated - in this example a random number
        int notificationId = (int) (System.currentTimeMillis() / 10000L);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);

        // trigger the notification.
        mNotificationManager.notify(notificationId, notification);
    }
}
