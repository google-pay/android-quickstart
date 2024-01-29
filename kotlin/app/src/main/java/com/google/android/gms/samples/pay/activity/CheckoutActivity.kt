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

package com.google.android.gms.samples.pay.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.contract.TaskResultContracts.GetPaymentDataResult
import com.google.android.gms.samples.pay.R
import com.google.android.gms.samples.pay.ui.ProductScreen
import com.google.android.gms.samples.pay.viewmodel.CheckoutViewModel
import com.google.android.gms.samples.pay.viewmodel.PaymentUiState
import com.google.android.gms.samples.pay.viewmodel.awaitTask
import kotlinx.coroutines.launch

class CheckoutActivity : ComponentActivity() {

    private val paymentDataLauncher = registerForActivityResult(GetPaymentDataResult()) {
        when (it.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                Log.i("Google Pay result:", it.result.toString())
                it.result?.let(model::setPaymentData)
            }
            //CommonStatusCodes.CANCELED -> The user canceled
            //AutoResolveHelper.RESULT_ERROR -> The API returned an error (it.status: Status)
            //CommonStatusCodes.INTERNAL_ERROR -> Handle other unexpected errors
        }
    }

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
                    lifecycleScope.launch {
                        val task = model.getLoadPaymentDataTask().awaitTask()
                        paymentDataLauncher.launch(task)
                    }
                },
            )
        }
    }
}