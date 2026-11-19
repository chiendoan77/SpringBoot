package com.example.reporting.dto;

import java.math.BigDecimal;

public class SummaryReportResponse {
    private long totalInventoryLevels;
    private long totalSalesVolume;
    private BigDecimal totalRevenue;
    private BigDecimal totalCosts;
    private BigDecimal netProfit;

    public SummaryReportResponse(long totalInventoryLevels, long totalSalesVolume, BigDecimal totalRevenue, BigDecimal totalCosts, BigDecimal netProfit) {
        this.totalInventoryLevels = totalInventoryLevels;
        this.totalSalesVolume = totalSalesVolume;
        this.totalRevenue = totalRevenue;
        this.totalCosts = totalCosts;
        this.netProfit = netProfit;
    }

    public long getTotalInventoryLevels() { return totalInventoryLevels; }
    public long getTotalSalesVolume() { return totalSalesVolume; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getTotalCosts() { return totalCosts; }
    public BigDecimal getNetProfit() { return netProfit; }
}
