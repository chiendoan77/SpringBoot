package com.example.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
        @NotNull @Positive Integer orderId,
        @NotNull @Positive Integer productId,
        @NotNull @Positive Integer quantity) {
}
