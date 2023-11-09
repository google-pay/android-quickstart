/*
 * Copyright 2023 Google Inc.
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

package com.google.android.gms.samples.wallet.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.samples.wallet.viewmodel.CheckoutViewModel
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.ui.ProductScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.samples.wallet.viewmodel.PaymentUiState
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData

class CheckoutActivity : ComponentActivity() {

    private val googlePayRequestCode = 1001

    private val model: CheckoutViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val payState: PaymentUiState by model.paymentUiState.collectAsStateWithLifecycle()
            ProductScreen(
                title = "Men's Tech Shell Full-Zip",
                description = "A versatile full-zip that you can wear all day long and even...",
                price = "$50.20",
                image = R.drawable.ts_10_11019a,
                payUiState = payState,
                onGooglePayButtonClick = {
                    AutoResolveHelper.resolveTask(
                        model.getLoadPaymentDataTask(), this, googlePayRequestCode)
                },
            )
        }
    }

    @Deprecated("Deprecated and in use by Google Pay")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == googlePayRequestCode) {
            when (resultCode) {
                RESULT_OK -> data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let(model::setPaymentData)
                }
                /* Handle other result scenarios
                 * Learn more at: https://developers.google.com/pay/api/android/support/troubleshooting
                 */
                else -> { // Other uncaught errors }
                }
            }
        }
    }
}
