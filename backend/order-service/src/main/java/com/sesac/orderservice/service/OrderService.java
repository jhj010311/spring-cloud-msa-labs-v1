package com.sesac.orderservice.service;

import com.sesac.orderservice.client.ProductServiceClient;
import com.sesac.orderservice.client.UserServiceClient;
import com.sesac.orderservice.client.dto.ProductDto;
import com.sesac.orderservice.client.dto.UserDto;
import com.sesac.orderservice.dto.OrderRequest;
import com.sesac.orderservice.entity.Order;
import com.sesac.orderservice.entity.OrderStatus;
import com.sesac.orderservice.event.OrderCreatedEvent;
import com.sesac.orderservice.event.OrderEventPublisher;
import com.sesac.orderservice.facade.UserServiceFacade;
import com.sesac.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceFacade userServiceFacade;
    private final Tracer tracer;
    private final OrderEventPublisher orderEventPublisher;

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("order not found"));
    }


    // 주문 생성
    // 유저 정보
    // 상품 정보

    @Transactional
    public Order createOrder(OrderRequest request) {

        Span span = tracer.nextSpan()
                .name("createOrder")
                .tag("order.userId", request.getUserId())
                .tag("order.productId", request.getProductId())
                .start();

        try(Tracer.SpanInScope ws = tracer.withSpan(span)) {
            UserDto user = userServiceFacade.getUserWithFallback(request.getUserId());
            if(user == null){
                throw new EntityNotFoundException("user not found");
            }

            ProductDto product = productServiceClient.getProductById(request.getProductId());
            if(product == null){
                throw new EntityNotFoundException("product not found");
            }

            Order order = new Order();

            order.setUserId(user.getId());
            order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            order.setStatus(OrderStatus.PENDING);


            // 이벤트 발행 전에 저장해서 이벤트에 값이 반영되도록 함
            Order savedOrder = orderRepository.save(order);


            // rabbitMQ 비동기 이벤트 발행
            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getCreatedAt()
            );

            orderEventPublisher.publishOrderCreated(event);


            return orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }

    public List<Order> getOrdersByUserId(Long userId){
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
