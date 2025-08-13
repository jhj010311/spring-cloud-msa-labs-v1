package com.sesac.productservice.event;

import com.sesac.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    // 해당하는 큐를 자동추적하여 발생
    @RabbitListener(queues = "${order.event.queue.inventory}")
    public void handleOrderEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId={}", event.getOrderId());

        try {
            productService.decreaseStock(event.getProductId(), event.getQuantity());
            log.info("주문 생성 및 재고 차감 완료 - orderId={}", event.getOrderId());
        } catch (RuntimeException e) {
            log.error("주문 생성 및 재고 차감 실패 - orderId={}", event.getOrderId());
        }
    }
}
