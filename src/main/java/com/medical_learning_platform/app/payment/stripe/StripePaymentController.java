package com.medical_learning_platform.app.payment.stripe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripePaymentController {

    private final StripePaymentService paymentService;

    // Пример: создаём Intent на фиксированную сумму (например, 20.00 USD)
    @PostMapping("/create-intent")
    public ResponseEntity<?> createIntent() throws Exception {
        long amount = 2000L;               // тут в реале считаем по заказу из БД
        String currency = "usd";
        var res = paymentService.createPaymentIntent(amount, currency, "Order #1234");
        return ResponseEntity.ok(res);
    }
}
