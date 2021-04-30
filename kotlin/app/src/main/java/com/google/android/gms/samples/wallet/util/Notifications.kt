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
package com.google.android.gms.samples.wallet.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.activity.PaymentTransparentActivity
import com.google.android.gms.samples.wallet.service.PaymentNotificationIntentService
import java.util.*

object Notifications {

  private const val NOTIFICATION_ID = 1
  private const val NOTIFICATION_CHANNEL_ID = "payments_channel"

  private const val OPTION_1 = "option1"
  private const val OPTION_2 = "option2"
  private const val OPTION_3 = "option3"

  const val ACTION_SELECT_PREFIX = "action_select:"
  const val ACTION_PAY_GOOGLE_PAY = "action_pay_google_pay"
  const val ACTION_PAY_OTHER = "action_pay_other"

  const val OPTION_PRICE_EXTRA = "optionPriceExtra"

  private val OPTION_BUTTONS = mapOf(
          OPTION_1 to "buttonOption1",
          OPTION_2 to "buttonOption2",
          OPTION_3 to "buttonOption3")

  private val OPTION_PRICE_CENTS = mapOf(
        OPTION_1 to 1000L,
        OPTION_2 to 2500L,
        OPTION_3 to 5000L)

  /**
   * Triggers/updates a payment notification
   */
  @JvmOverloads
  fun triggerPaymentNotification(context: Context, selectedOption: String = OPTION_2) {
    val res = context.resources
    val packageName = context.packageName

    // Create a custom notification layout
    val notificationLayout = RemoteViews(packageName, R.layout.large_notification)

    // Creates the selectable options
    val options: List<String> = ArrayList(OPTION_PRICE_CENTS.keys)
    for (option in options) {

      // Adjust color based on selected option
      var optionColor = res.getColor(R.color.price_button_grey, context.theme)
      var optionBg: Int = R.drawable.price_button_background
      if (option == selectedOption) {
        optionColor = Color.WHITE
        optionBg = R.drawable.price_button_background_selected
      }

      val buttonId = res.getIdentifier(OPTION_BUTTONS[option], "id", packageName)
      notificationLayout.setTextColor(buttonId, optionColor)
      notificationLayout.setInt(buttonId, "setBackgroundResource", optionBg)

      // Create pendingIntent to respond to the click event
      val selectOptionIntent = Intent(context, PaymentNotificationIntentService::class.java)
      selectOptionIntent.action = ACTION_SELECT_PREFIX + option
      notificationLayout.setOnClickPendingIntent(buttonId, PendingIntent.getService(
              context, 0, selectOptionIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    // Set Google Pay button action
    val payIntent = Intent(context, PaymentTransparentActivity::class.java)
    payIntent.action = ACTION_PAY_GOOGLE_PAY
    payIntent.putExtra(OPTION_PRICE_EXTRA, OPTION_PRICE_CENTS[selectedOption])
    notificationLayout.setOnClickPendingIntent(
            R.id.googlePayButton, pendingIntentForActivity(context, payIntent))

    // Create a notification and set the notification channel
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setCustomBigContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .build()

    // Trigger or update the notification.
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
  }

  fun remove(context: Context) {
    NotificationManagerCompat.from(context).cancelAll()
  }

  /**
   * Create the notification channel for API 26+, to help the system group notifications
   * based on logical groups that users can make sense of and manage collectively.
   *
   * @param context where the channel is being created from
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
  fun createNotificationChannelIfNotCreated(context: Context) {
    val name: CharSequence = context.getString(R.string.channel_name)
    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            name, NotificationManager.IMPORTANCE_HIGH)
    val notificationMgr = context.getSystemService(NotificationManager::class.java)
    notificationMgr.createNotificationChannel(channel)
  }

  private fun pendingIntentForActivity(context: Context, intent: Intent): PendingIntent {
    return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }
}