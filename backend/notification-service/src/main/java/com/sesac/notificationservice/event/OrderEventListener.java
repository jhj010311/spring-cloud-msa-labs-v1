package com.sesac.notificationservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    // 해당하는 큐를 자동추적하여 발생
    @RabbitListener(queues = "${order.event.queue.notification}")
    public void handleOrderEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId={}", event.getOrderId());

        try {
            log.info("이메일 발송 완료 - orderId={}", event.getOrderId());
        } catch (RuntimeException e) {
            log.error("이메일 발송 실패 - orderId={}", event.getOrderId());
        }
    }
}
