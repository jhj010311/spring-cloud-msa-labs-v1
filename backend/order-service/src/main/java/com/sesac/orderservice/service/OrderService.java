package com.sesac.orderservice.service;

import com.sesac.orderservice.client.ProductServiceClient;
import com.sesac.orderservice.client.UserServiceClient;
import com.sesac.orderservice.client.dto.ProductDto;
import com.sesac.orderservice.client.dto.UserDto;
import com.sesac.orderservice.dto.OrderRequest;
import com.sesac.orderservice.entity.Order;
import com.sesac.orderservice.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("order not found"));
    }


    // 주문 생성
    // 유저 정보
    // 상품 정보

    @Transactional
    public Order createOrder(OrderRequest request) {

        UserDto user = userServiceClient.getUserById(request.getUserId());
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }

        ProductDto product = productServiceClient.getProductById(request.getProductId());
        if(product == null){
            throw new EntityNotFoundException("product not found");
        }

        if(product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("out of stock");
        }

        Order order = new Order();

        order.setUserId(user.getId());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("COMPLETED");

        return orderRepository.save(order);
    }
}
