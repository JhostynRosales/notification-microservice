package com.jhostyn.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDTO {
    private String orderId;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDTO> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String sku;
        private Integer quantity;
        private String productName;
    }
}
