package com.example.reporting.controller;

import com.example.reporting.dto.OrderReportResponse;
import com.example.reporting.dto.ProductReportResponse;
import com.example.reporting.dto.SummaryReportResponse;
import com.example.reporting.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryReportResponse> getSummaryReport() {
        return ResponseEntity.ok(reportingService.getSummary());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductReportResponse>> getProductReports() {
        return ResponseEntity.ok(reportingService.getProductReports());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderReportResponse>> getOrderReports() {
        return ResponseEntity.ok(reportingService.getOrderReports());
    }
}
