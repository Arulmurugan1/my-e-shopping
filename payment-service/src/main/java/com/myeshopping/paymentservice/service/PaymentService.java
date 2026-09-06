package com.myeshopping.paymentservice.service;

import com.myeshopping.paymentservice.dto.PaymentRequest;
import com.myeshopping.paymentservice.entity.Payment;
import com.myeshopping.paymentservice.entity.PaymentStatus;
import com.myeshopping.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private static final Map<PaymentStatus, Set<PaymentStatus>> TRANSITIONS = Map.of(
            PaymentStatus.INITIATED, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.REFUND_PENDING),
            PaymentStatus.SUCCESS, Set.of(PaymentStatus.REFUND_PENDING),
            PaymentStatus.FAILED, Set.of(PaymentStatus.REFUND_CANCELLED),
            PaymentStatus.REFUND_PENDING, Set.of(PaymentStatus.REFUNDED, PaymentStatus.REFUND_FAILED));

    @Transactional
    public Payment initiate(PaymentRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key is required");
        return paymentRepository.findByIdempotencyKey(idempotencyKey).orElseGet(() -> paymentRepository.save(Payment.builder()
                .orderId(request.getOrderId()).customerId(request.getCustomerId()).amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase()).idempotencyKey(idempotencyKey).status(PaymentStatus.INITIATED).build()));
    }

    @Transactional(readOnly = true)
    public Payment get(Long id) { return paymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id)); }

    @Transactional
    public Payment transition(Long id, String targetText) {
        Payment payment = get(id);
        PaymentStatus target;
        try { target = PaymentStatus.valueOf(targetText); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown payment status: " + targetText); }
        if (!TRANSITIONS.getOrDefault(payment.getStatus(), Set.of()).contains(target)) {
            throw new IllegalArgumentException("Invalid payment transition from " + payment.getStatus() + " to " + target);
        }
        payment.setStatus(target);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment requestRefund(Long id) { return transition(id, PaymentStatus.REFUND_PENDING.name()); }
}
