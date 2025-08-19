package com.sesac.productservice.event;

import com.sesac.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSagaHandler {

    private final ProductService productService;
    private final ProductSagaPublisher productSagaPublisher;

    // 해당하는 큐를 자동추적하여 발생
    @RabbitListener(queues = "${order.event.queue.inventory}")
    public void handleOrderEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId={}", event.getOrderId());

        try {
            productService.decreaseStock(event.getProductId(), event.getQuantity());
            log.info("주문 생성 및 재고 차감 완료 - orderId={}", event.getOrderId());
            
            
            // Saga pattern : payment-service에 paymentRequestEvent 발행
            PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                    event.getOrderId(),
                    event.getUserId(), 
                    event.getProductId(),
                    event.getQuantity(),
                    event.getTotalAmount()
            );
            
            productSagaPublisher.publishPaymentRequest(paymentRequestEvent);
            
        } catch (RuntimeException e) {
            log.error("주문 생성 및 재고 차감 실패 - orderId={}", event.getOrderId());
            
            
            // Saga pattern : order-service에 inventoryFailedEvent 발행
            InventoryFailedEvent inventoryFailedEvent = new InventoryFailedEvent(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    "재고 부족"
            );

            productSagaPublisher.publishInventoryFailed(inventoryFailedEvent);
        }
    }


    // 결제 실패 이벤트 발생시 재고를 되돌리는 이벤트도 발행됨
    @RabbitListener(queues = "${order.event.queue.inventory-restore}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, reason: {}", event.getOrderId(), event.getReason());

        try {
            productService.restoreStock(event.getProductId(), event.getQuantity());
            log.info("재고 복구 완료 (보상 트랜잭션) - orderId: {}, productId{}, quantity: {}"
                    , event.getOrderId(), event.getProductId(), event.getQuantity());

        } catch (Exception e) {
            log.error("재고 복구 실패 - orderId: {}, error: {}", event.getOrderId(), e.getMessage());
        }
    }
}
