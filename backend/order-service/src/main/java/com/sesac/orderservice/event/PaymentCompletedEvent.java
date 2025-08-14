package com.sesac.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    // 직렬화 용도

    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
}
