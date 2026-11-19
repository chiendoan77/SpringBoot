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
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class OrderItemDto {
        private Integer id;
        private Integer orderId;
        private Integer productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getOrderId() { return orderId; }
        public void setOrderId(Integer orderId) { this.orderId = orderId; }
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public BigDecimal getPrice() { return unitPrice != null ? unitPrice : BigDecimal.ZERO; }
    }

    public static class OrderDto {
        private Integer id;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalAmount;
        private String status;
        private List<OrderItemDto> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<OrderItemDto> getItems() { return items; }
        public void setItems(List<OrderItemDto> items) { this.items = items; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}
