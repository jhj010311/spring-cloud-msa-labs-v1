package com.sesac.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaHandler {

    // 수신할 이벤트 목록
    // InventoryFailedEvent
    // PaymentCompletedEvent
    // PaymentFailedEvent

    @RabbitListener(queues = "order.event.queue.inventory-failed")
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.info("재고 부족 이벤트 수신 - orderId: {}, reason: {}", event.getOrderId(), event.getReason());
    }

    @RabbitListener(queues = "order.event.queue.payment-completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 성공 이벤트 수신 - orderId: {}, amount: {}", event.getOrderId(), event.getTotalAmount());
    }

    @RabbitListener(queues = "order.event.queue.payment-failed")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, reason: {}", event.getOrderId(), event.getReason());
    }
}
