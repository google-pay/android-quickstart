/*
 * Copyright 2022 Google Inc.
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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutBinding
import java.util.*
import kotlin.random.Random

/**
 * Checkout implementation for the app
 */
class CheckoutActivity : AppCompatActivity() {

    // 6.1 Add a request code for the save operation

    private lateinit var layout: ActivityCheckoutBinding
    private lateinit var addToGoogleWalletButton: View

    // 3.1. Create a member with a client to interact with the Google Wallet API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3.2. Instantiate the Pay client

        // Use view binding to access the UI elements
        layout = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(layout.root)

        // 6.2 Set a click listener on the "Add to Google Wallet" button

        // 4.2. Trigger the API availability request
    }

    // 4.1. Create a method to check whether the Google Wallet API is available and respond to the result

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 7. Handle the result
    }

    // 5. Add the pass object definition with your own issuer id and class id
}
