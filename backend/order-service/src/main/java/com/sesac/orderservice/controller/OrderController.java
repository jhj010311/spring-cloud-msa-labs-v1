package com.sesac.orderservice.controller;

import com.sesac.orderservice.dto.OrderRequest;
import com.sesac.orderservice.entity.Order;
import com.sesac.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "주문 컨트롤러")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    @Operation(summary = "주문 조회", description = "ID로 주문 정보를 조회합니다")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.findById(id);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    @Operation(summary = "내 주문 목록", description = "로그인한 사용자의 주문 목록을 조회합니다")
    public ResponseEntity<List<Order>> getMyOrders(HttpServletRequest request) {
        // api gateway에서 전달한 X-User-Id 헤더에서 사용자 id 추출
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Order> orders = orderService.getOrdersByUserId(Long.parseLong(userIdHeader));

        return ResponseEntity.ok(orders);
    }
}
