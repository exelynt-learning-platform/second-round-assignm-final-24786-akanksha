package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class CreateOrderRequest {
        @NotBlank
        private String shippingAddress;
    }

    @Data
    public static class OrderItemResponse {
        private String productName;
        private Integer quantity;
        private Double price;
    }

    @Data
    public static class OrderResponse {
        private Long orderId;
        private List<OrderItemResponse> items;
        private Double totalPrice;
        private String shippingAddress;
        private String paymentStatus;
        private LocalDateTime createdAt;
        private String clientSecret;
    }
}
