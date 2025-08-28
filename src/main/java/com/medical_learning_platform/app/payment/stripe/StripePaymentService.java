package com.medical_learning_platform.app.payment.stripe;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class StripePaymentService {


    public StripeIntentResult createPaymentIntent(long amountMinor, String currency, String description) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountMinor)             // minor units: 2000 = $20.00, для ILS: 100 агорот = 1₪
            .setCurrency(currency)              // "usd", "eur", "ils" (в тесте можно)
            .setDescription(description)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .build();

        PaymentIntent intent = PaymentIntent.create(params,
                new com.stripe.net.RequestOptions.RequestOptionsBuilder()
                        .setIdempotencyKey("pi-" + UUID.randomUUID()).build());

        return new StripeIntentResult(intent.getClientSecret());
    }
}
