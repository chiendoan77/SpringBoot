package com.example.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderReportResponse {
    private Integer orderId;
    private String customerName;
    private String customerEmail;
    private String status;
    private LocalDateTime createdAt;
    private int totalItemsCount;
    private BigDecimal revenue;
    private BigDecimal costs;
    private BigDecimal netProfit;

    public OrderReportResponse(Integer orderId, String customerName, String customerEmail, String status, LocalDateTime createdAt, int totalItemsCount, BigDecimal revenue, BigDecimal costs, BigDecimal netProfit) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.status = status;
        this.createdAt = createdAt;
        this.totalItemsCount = totalItemsCount;
        this.revenue = revenue;
        this.costs = costs;
        this.netProfit = netProfit;
    }

    public Integer getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getTotalItemsCount() { return totalItemsCount; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getCosts() { return costs; }
    public BigDecimal getNetProfit() { return netProfit; }
}
