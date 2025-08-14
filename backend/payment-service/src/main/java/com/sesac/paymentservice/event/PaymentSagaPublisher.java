package com.sesac.paymentservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${order.event.exchange}")
    private String exchange;

    @Value("${order.event.routing-key.payment-completed}")
    private String paymentCompletedRoutingKey;

    @Value("${order.event.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    @Value("${order.event.routing-key.inventory-restore}")
    private String inventoryRestoreRoutingKey;


    // 결제 성공시
    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(exchange, paymentCompletedRoutingKey, event);
    }

    // 결제 실패시
    public void publishPaymentFailureEvent(PaymentFailedEvent event) {
        // order-service에게 결제 실패 이벤트 발행(주문 취소)
        rabbitTemplate.convertAndSend(exchange, paymentFailedRoutingKey, event);

        // product-service에게 결제 실패 이벤트 발행(재고 복구)
        rabbitTemplate.convertAndSend(exchange, inventoryRestoreRoutingKey, event);
    }
}
