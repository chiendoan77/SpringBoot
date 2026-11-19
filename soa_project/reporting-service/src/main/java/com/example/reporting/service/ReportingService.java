package com.example.reporting.service;

import com.example.reporting.client.ExternalData;
import com.example.reporting.dto.OrderReportResponse;
import com.example.reporting.dto.ProductReportResponse;
import com.example.reporting.dto.SummaryReportResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String productServiceUrl;
    private final String orderServiceUrl;

    public ReportingService(
            @Value("${product.service.url:http://localhost:8085}") String productServiceUrl,
            @Value("${order.service.url:http://localhost:8088}") String orderServiceUrl) {
        this.productServiceUrl = productServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
    }

    private List<ExternalData.ProductDto> fetchProducts() {
        try {
            ResponseEntity<List<ExternalData.ProductDto>> response = restTemplate.exchange(
                    productServiceUrl + "/products",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ExternalData.ProductDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<ExternalData.OrderDto> fetchOrders() {
        try {
            ResponseEntity<List<ExternalData.OrderDto>> response = restTemplate.exchange(
                    orderServiceUrl + "/orders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ExternalData.OrderDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public SummaryReportResponse getSummary() {
        List<ExternalData.ProductDto> products = fetchProducts();
        List<ExternalData.OrderDto> orders = fetchOrders();

        long totalInventory = products.stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum();
        long totalSalesVolume = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCosts = BigDecimal.ZERO;

        for (ExternalData.OrderDto order : orders) {
            String status = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
            if ("completed".equals(status)) {
                if (order.getItems() != null) {
                    for (ExternalData.OrderItemDto item : order.getItems()) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                        BigDecimal rev = price.multiply(BigDecimal.valueOf(qty));
                        
                        totalSalesVolume += qty;
                        totalRevenue = totalRevenue.add(rev);
                        BigDecimal cost = rev.multiply(BigDecimal.valueOf(0.60));
                        totalCosts = totalCosts.add(cost);
                    }
                }
            }
        }

        BigDecimal netProfit = totalRevenue.subtract(totalCosts);

        return new SummaryReportResponse(
                totalInventory,
                totalSalesVolume,
                totalRevenue,
                totalCosts,
                netProfit
        );
    }

    public List<ProductReportResponse> getProductReports() {
        List<ExternalData.ProductDto> products = fetchProducts();
        List<ExternalData.OrderDto> orders = fetchOrders();

        Map<Integer, Long> salesVolumeMap = new HashMap<>();
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();

        for (ExternalData.OrderDto order : orders) {
            String status = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
            if ("completed".equals(status)) {
                if (order.getItems() != null) {
                    for (ExternalData.OrderItemDto item : order.getItems()) {
                        if (item.getProductId() != null) {
                            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                            BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                            BigDecimal rev = price.multiply(BigDecimal.valueOf(qty));

                            salesVolumeMap.put(item.getProductId(), salesVolumeMap.getOrDefault(item.getProductId(), 0L) + qty);
                            revenueMap.put(item.getProductId(), revenueMap.getOrDefault(item.getProductId(), BigDecimal.ZERO).add(rev));
                        }
                    }
                }
            }
        }

        List<ProductReportResponse> reports = new ArrayList<>();
        for (ExternalData.ProductDto p : products) {
            long vol = salesVolumeMap.getOrDefault(p.getId(), 0L);
            BigDecimal rev = revenueMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal cost = rev.multiply(BigDecimal.valueOf(0.60));
            BigDecimal profit = rev.subtract(cost);
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;

            reports.add(new ProductReportResponse(
                    p.getId(),
                    p.getName(),
                    qty,
                    vol,
                    rev,
                    cost,
                    profit
            ));
        }

        return reports;
    }

    public List<OrderReportResponse> getOrderReports() {
        List<ExternalData.OrderDto> orders = fetchOrders();
        List<OrderReportResponse> reports = new ArrayList<>();

        for (ExternalData.OrderDto order : orders) {
            String status = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
            if ("completed".equals(status)) {
                int totalItems = 0;
                BigDecimal revenue = BigDecimal.ZERO;

                if (order.getItems() != null) {
                    for (ExternalData.OrderItemDto item : order.getItems()) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                        totalItems += qty;
                        revenue = revenue.add(price.multiply(BigDecimal.valueOf(qty)));
                    }
                }

                BigDecimal costs = revenue.multiply(BigDecimal.valueOf(0.60));
                BigDecimal netProfit = revenue.subtract(costs);

                reports.add(new OrderReportResponse(
                        order.getId(),
                        order.getCustomerName(),
                        order.getCustomerEmail(),
                        order.getStatus(),
                        order.getCreatedAt(),
                        totalItems,
                        revenue,
                        costs,
                        netProfit
                ));
            }
        }

        return reports;
    }
}
