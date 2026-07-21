/*
 * Copyright 2024 Google Inc.
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

package com.google.android.gms.samples.pay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.samples.pay.R
import com.google.android.gms.samples.pay.util.PaymentsUtil
import com.google.android.gms.samples.pay.viewmodel.PaymentUiState
import com.google.pay.button.PayButton

@Composable
fun ProductScreen(
    title: String,
    description: String,
    price: String,
    image: Int,
    onGooglePayButtonClick: () -> Unit,
    payUiState: PaymentUiState = PaymentUiState.NotStarted,
    onPaymentComplete: (String) -> Unit,
) {
    val padding = 20.dp
    val black = Color(0xff000000.toInt())
    val grey = Color(0xffeeeeee.toInt())

    Column(
        modifier = Modifier
            .background(grey)
            .padding(padding)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(space = padding / 2),
        ) {
            Image(
                contentDescription = null,
                painter = painterResource(image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )
            Text(
                text = title,
                color = black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = price, color = black)
            Spacer(Modifier)
            Text(
                text = "Description",
                color = black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = black
            )
            if (payUiState !is PaymentUiState.NotStarted) {
                PayButton(
                    modifier = Modifier
                        .testTag("payButton")
                        .fillMaxWidth(),
                    onClick = onGooglePayButtonClick,
                    allowedPaymentMethods = PaymentsUtil.allowedPaymentMethods.toString()
                )
            }
        }

    LaunchedEffect(key1 = payUiState) {
        if (payUiState is PaymentUiState.PaymentCompleted) {
            onPaymentComplete(payUiState.payerName)
        }
    }

    }


/**
 * Wrapper to simplify previews with a provided description.
 */
@Composable
private fun ProductScreenPreviewWithDescription(
    payUiState: PaymentUiState,
    description: String = "This is a product description."
) {
    ProductScreen(
        title = "Men's Tech Shell Full-Zip",
        description = description.take(200), // Limit description to 200 characters
        price = "$49.99",
        image = R.drawable.ts_10_11019a,
        onGooglePayButtonClick = {}, // No-op for previews
        payUiState = payUiState,
        onPaymentComplete = {} // No Navigation for preview
    )
}

/**
 * Preview of ProductScreen in the initial state, where payment has not started.
 */
@Preview
@Composable
private fun ProductScreenPreviewNotStarted() {
    ProductScreenPreviewWithDescription(
        payUiState = PaymentUiState.NotStarted,
        description = "This is a product description."
    )
}

/**
 * Preview of ProductScreen when Google Pay is available.
 */
@Preview
@Composable
private fun ProductScreenPreviewAvailable() {
    ProductScreenPreviewWithDescription(
        payUiState = PaymentUiState.Available,
        description = "This is a product description."
    )
}

/**
 * Preview of ProductScreen after payment has been completed.
 */
@Preview
@Composable
private fun ProductScreenPreviewPaymentCompleted() {
    ProductScreenPreviewWithDescription(
        payUiState = PaymentUiState.PaymentCompleted(payerName = "John"),
        description = "This is a product description."
    )
}
