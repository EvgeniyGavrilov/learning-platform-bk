package com.medical_learning_platform.app.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.stripe.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestBody String payload,
                                         @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Webhook");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }
//        log.info(event.toString());
        switch (event.getType()) {
            case "checkout.session.async_payment_failed": {
                log.info("async_payment_failed");
                // Then define and call a function to handle the event checkout.session.async_payment_failed
                break;
            }
            case "checkout.session.async_payment_succeeded": {
                log.info("async_payment_succeeded");
                // Then define and call a function to handle the event checkout.session.async_payment_succeeded
                break;
            }
            case "checkout.session.completed": {
                log.info("completed");
                // Then define and call a function to handle the event checkout.session.completed
                break;
            }
            case "payment_intent.created": {
                log.info("created");
                // Then define and call a function to handle the event payment_intent.payment_failed
                break;
            }
            case "payment_intent.payment_failed": {
                log.info("payment_failed");
                // Then define and call a function to handle the event payment_intent.payment_failed
                break;
            }
            case "payment_intent.succeeded": {
                log.info("succeeded");
                // Then define and call a function to handle the event payment_intent.succeeded
                break;
            }
            // ... handle other event types
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
        return ResponseEntity.ok("ok");
    }
}
