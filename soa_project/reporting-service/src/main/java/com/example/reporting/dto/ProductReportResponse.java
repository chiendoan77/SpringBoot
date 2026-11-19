package com.example.reporting.dto;

import java.math.BigDecimal;

public class ProductReportResponse {
    private Integer productId;
    private String productName;
    private int currentInventory;
    private long salesVolume;
    private BigDecimal revenue;
    private BigDecimal costs;
    private BigDecimal netProfit;

    public ProductReportResponse(Integer productId, String productName, int currentInventory, long salesVolume, BigDecimal revenue, BigDecimal costs, BigDecimal netProfit) {
        this.productId = productId;
        this.productName = productName;
        this.currentInventory = currentInventory;
        this.salesVolume = salesVolume;
        this.revenue = revenue;
        this.costs = costs;
        this.netProfit = netProfit;
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getCurrentInventory() { return currentInventory; }
    public long getSalesVolume() { return salesVolume; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getCosts() { return costs; }
    public BigDecimal getNetProfit() { return netProfit; }
}
