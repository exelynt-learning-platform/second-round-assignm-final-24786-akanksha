package com.ecommerce.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.ecommerce.entity.Order;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createPaymentIntent(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (order.getTotalPrice() * 100))
                    .setCurrency("usd")
                    .putMetadata("orderId", orderId.toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            order.setStripePaymentIntentId(intent.getId());
            orderRepository.save(order);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw new ApiException("Payment initiation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            throw new ApiException("Invalid webhook signature", HttpStatus.BAD_REQUEST);
        }

        Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isEmpty()) return;

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) stripeObject.get();
            updateOrderStatus(intent.getMetadata().get("orderId"), Order.PaymentStatus.PAID);
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) stripeObject.get();
            updateOrderStatus(intent.getMetadata().get("orderId"), Order.PaymentStatus.FAILED);
        }
    }

    private void updateOrderStatus(String orderId, Order.PaymentStatus status) {
        if (orderId == null) return;
        orderRepository.findById(Long.parseLong(orderId)).ifPresent(order -> {
            order.setPaymentStatus(status);
            orderRepository.save(order);
        });
    }
}
