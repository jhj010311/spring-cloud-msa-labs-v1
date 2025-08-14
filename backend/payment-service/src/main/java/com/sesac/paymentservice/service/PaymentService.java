package com.sesac.paymentservice.service;

import com.sesac.paymentservice.entity.Payment;
import com.sesac.paymentservice.entity.PaymentStatus;
import com.sesac.paymentservice.event.PaymentRequestEvent;
import com.sesac.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPayment(PaymentRequestEvent event) {
        Payment payment = new Payment();

        payment.setOrderId(event.getOrderId());
        payment.setUserId(event.getUserId());
        payment.setAmount(event.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("CARD");

        Payment savedPayment = paymentRepository.save(payment);

        // 결제 처리 시뮬레이션
        try {
            if(Math.random() < 0.3) {
                throw new RuntimeException("잔액 부족");
            }

            savedPayment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(savedPayment);
        } catch (RuntimeException e) {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason(e.getMessage());
            paymentRepository.save(savedPayment);
        }

        return savedPayment;
    }
}
