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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.ui.ProductScreen
import com.google.android.gms.samples.wallet.viewmodel.CheckoutViewModel

class CheckoutActivity : ComponentActivity() {

    private val addToGoogleWalletRequestCode = 1000
    private val model: CheckoutViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProductScreen(
                title = "Men's Tech Shell Full-Zip",
                description = "A versatile full-zip that you can wear all day long and even...",
                price = "$50.20",
                image = R.drawable.ts_10_11019a,
                viewModel = model,
                googleWalletButtonOnClick = { requestSavePass() },
            )
        }
    }

    private fun requestSavePass() {
        // Disables the button to prevent multiple clicks.
        model.setGoogleWalletButtonClickable(false)
        model.savePassesJwt(model.genericObjectJwt, this, addToGoogleWalletRequestCode)
    }

    @Deprecated("Deprecated and in use by Google Pay")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == addToGoogleWalletRequestCode) {
            when (resultCode) {
                RESULT_OK -> Toast.makeText(
                        this, getString(R.string.add_google_wallet_success), Toast.LENGTH_LONG
                    ).show()

                /* Handle other result scenarios
                 * Learn more at: https://developers.google.com/wallet/generic/android#5_add_the_object_to
                 */
                else -> { // Other uncaught errors }
                }
            }

            // Re-enables the Google Pay payment button.
            model.setGoogleWalletButtonClickable(true)
        }
    }
}