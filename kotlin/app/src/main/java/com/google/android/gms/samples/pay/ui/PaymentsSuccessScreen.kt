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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.samples.pay.R

@Composable
fun PaymentSuccessScreen(payerName: String) {
    val padding = 20.dp
    val grey = Color(0xffeeeeee.toInt())

    Column(
        modifier = Modifier
            .testTag("successScreen")
            .background(grey)
            .padding(padding)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            contentDescription = null,
            painter = painterResource(R.drawable.check_circle),
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "$payerName completed a payment.\nWe are preparing your order.",
            fontSize = 17.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}