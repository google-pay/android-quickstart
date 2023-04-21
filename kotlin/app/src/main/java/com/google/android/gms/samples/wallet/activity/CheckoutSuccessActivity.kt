package com.google.android.gms.samples.wallet.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutSuccessBinding

class CheckoutSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = ActivityCheckoutSuccessBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }
}