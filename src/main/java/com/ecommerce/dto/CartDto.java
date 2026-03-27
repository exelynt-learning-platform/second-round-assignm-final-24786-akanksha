package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class CartDto {

    @Data
    public static class AddItemRequest {
        @NotNull
        private Long productId;
        @NotNull @Min(1)
        private Integer quantity;
    }

    @Data
    public static class CartItemResponse {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }

    @Data
    public static class CartResponse {
        private Long cartId;
        private java.util.List<CartItemResponse> items;
        private Double total;
    }
}
