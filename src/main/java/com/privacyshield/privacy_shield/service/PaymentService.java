package com.privacyshield.privacy_shield.service;



import com.privacyshield.privacy_shield.dto.CreateOrderRequest;
import com.privacyshield.privacy_shield.dto.PaymentVerifyRequest;
import com.privacyshield.privacy_shield.entity.Payment;
import com.privacyshield.privacy_shield.entity.Subscription;
import com.privacyshield.privacy_shield.entity.User;
import com.privacyshield.privacy_shield.repository.PaymentRepository;
import com.privacyshield.privacy_shield.repository.SubscriptionRepository;
import com.privacyshield.privacy_shield.repository.UserRepository;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // Plan prices — paise mein
    private static final Map<String, Integer> PRICES
            = new HashMap<>();

    static {
        PRICES.put("PRO_SUBSCRIPTION",   99900);  // ₹999/month
        PRICES.put("PRO_ONETIME",        99900);  // ₹999 one-time
        PRICES.put("ENTERPRISE_SUBSCRIPTION", 499900); // ₹4999/month
        PRICES.put("ENTERPRISE_ONETIME", 499900); // ₹4999 one-time
    }

    // ========================================
    // ORDER CREATE KARO
    // ========================================
    public Map<String, Object> createOrder(
            CreateOrderRequest req) throws Exception {

        // Price calculate karo
        String priceKey = req.getPlanType()
                + "_" + req.getPaymentType();
        Integer amount = PRICES.getOrDefault(
                priceKey, 99900);

        // Razorpay client banao
        RazorpayClient client = new RazorpayClient(
                keyId, keySecret);

        // Order options
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt",
                "order_" + System.currentTimeMillis());

        // Notes — extra info
        JSONObject notes = new JSONObject();
        notes.put("plan", req.getPlanType());
        notes.put("type", req.getPaymentType());
        notes.put("email", req.getEmail());
        orderRequest.put("notes", notes);

        // Razorpay se order create karo
        Order order = client.orders.create(orderRequest);

        // Database mein save karo
        User user = userRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User nahi mila"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setRazorpayOrderId(order.get("id"));
        payment.setAmount(amount);
        payment.setPlanType(req.getPlanType());
        payment.setPaymentType(req.getPaymentType());
        payment.setStatus("CREATED");
        paymentRepository.save(payment);

        // Frontend ko response bhejo
        Map<String, Object> response = new HashMap<>();
        response.put("orderId",   order.get("id"));
        response.put("amount",    amount);
        response.put("currency",  "INR");
        response.put("keyId",     keyId);
        response.put("planType",  req.getPlanType());
        return response;
    }

    // ========================================
    // PAYMENT VERIFY KARO
    // ========================================
    public Map<String, String> verifyPayment(
            PaymentVerifyRequest req) throws Exception {

        // Signature verify karo — security ke liye
        String data = req.getRazorpayOrderId()
                + "|" + req.getRazorpayPaymentId();

        String generatedSig = hmacSHA256(
                data, keySecret);

        if (!generatedSig.equals(
                req.getRazorpaySignature())) {
            throw new RuntimeException(
                    "Payment signature invalid! Fraud attempt.");
        }

        // Payment database mein update karo
        Payment payment = paymentRepository
                .findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order nahi mila"));

        payment.setRazorpayPaymentId(
                req.getRazorpayPaymentId());
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // User ka plan upgrade karo
        User user = payment.getUser();
        user.setPlan(payment.getPlanType());
        userRepository.save(user);

        // Subscription update karo
        updateSubscription(user, payment);

        Map<String, String> response = new HashMap<>();
        response.put("status",  "SUCCESS");
        response.put("message", "Payment successful! "
                + payment.getPlanType() + " plan active ho gaya!");
        response.put("plan", payment.getPlanType());
        return response;
    }

    // ========================================
    // SUBSCRIPTION UPDATE
    // ========================================
    private void updateSubscription(
            User user, Payment payment) {

        Subscription sub = subscriptionRepository
                .findByUser(user)
                .orElse(new Subscription());

        sub.setUser(user);
        sub.setPlan(payment.getPlanType());
        sub.setIsActive(true);

        // Monthly subscription ke liye 30 din add karo
        if ("SUBSCRIPTION".equals(
                payment.getPaymentType())) {
            sub.setValidUntil(
                    LocalDateTime.now().plusDays(30));
        } else {
            // One-time ke liye 365 din
            sub.setValidUntil(
                    LocalDateTime.now().plusDays(365));
        }

        subscriptionRepository.save(sub);
    }

    // ========================================
    // HMAC SHA256 — Signature verify ke liye
    // ========================================
    private String hmacSHA256(
            String data, String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes("UTF-8"), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(
                data.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // ========================================
// USER KA PLAN STATUS CHECK KARO
// ========================================
    public Map<String, String> getPlanStatus(
            String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User nahi mila"));

        Map<String, String> status = new HashMap<>();
        status.put("email", user.getEmail());
        status.put("plan", user.getPlan());
        status.put("documentsUsed",
                String.valueOf(user.getDocumentsUsed()));

        // Subscription details
        subscriptionRepository.findByUser(user)
                .ifPresent(sub -> {
                    status.put("isActive",
                            String.valueOf(sub.getIsActive()));
                    status.put("validUntil",
                            sub.getValidUntil() != null
                                    ? sub.getValidUntil().toString()
                                    : "N/A");
                });

        return status;
    }
}
