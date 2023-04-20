package com.google.android.gms.samples.wallet.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.samples.wallet.databinding.ActivityCheckoutSuccessBinding

class CheckoutSuccessActivity : AppCompatActivity() {

    private lateinit var layout: ActivityCheckoutSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityCheckoutSuccessBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }
}