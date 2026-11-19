package com.example.reporting.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExternalData {
    public static class ProductDto {
        private Integer id;
        private String name;
        private BigDecimal price;
        private Integer quantity;

        public Integer getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public Integer getQuantity() { return quantity; }
    }

    public static class OrderItemDto {
        private Integer productId;
        private Integer quantity;
        private BigDecimal price;

        public Integer getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }

    public static class OrderDto {
        private Integer id;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime createdAt;
        private List<OrderItemDto> items;

        public Integer getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public List<OrderItemDto> getItems() { return items; }
    }
}
