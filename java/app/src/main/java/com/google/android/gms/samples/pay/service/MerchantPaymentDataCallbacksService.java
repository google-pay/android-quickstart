package com.google.android.gms.samples.pay.service;

import androidx.annotation.NonNull;

import com.google.android.gms.wallet.callback.BasePaymentDataCallbacks;
import com.google.android.gms.samples.pay.activity.MerchantPaymentDataCallbacks;
import com.google.android.gms.wallet.callback.BasePaymentDataCallbacksService;


/**
 * Service class which hosts the payment data callbacks initiated by Google Play services
 * within a {@link PaymentsClient#loadPaymentData(PaymentDataRequest)}.
 *
 * <p>The callbacks are implemented through MerchantPaymentDataCallbacks returned from {@link #createPaymentDataCallbacks()}.
 */
public class MerchantPaymentDataCallbacksService extends BasePaymentDataCallbacksService {

    @NonNull
    @Override
    protected BasePaymentDataCallbacks createPaymentDataCallbacks() {
        return new MerchantPaymentDataCallbacks();
    }
}
