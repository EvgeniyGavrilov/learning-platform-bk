package com.medical_learning_platform.app.payment.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripeIntentResult {
    String clientSecret;
}
