package com.example.gateway;

import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class GatewayController {
    private final RestClient restClient;
    private final String authUrl;
    private final String productUrl;
    private final String orderUrl;
    private final String reportingUrl;

    public GatewayController(
            @Value("${gateway.auth-url}") String authUrl,
            @Value("${gateway.product-url:http://localhost:8085}") String productUrl,
            @Value("${gateway.order-url}") String orderUrl,
            @Value("${gateway.reporting-url:http://localhost:8089}") String reportingUrl) {
        this.restClient = RestClient.builder().build();
        this.authUrl = authUrl;
        this.productUrl = productUrl;
        this.orderUrl = orderUrl;
        this.reportingUrl = reportingUrl;
    }

    @PostMapping("/auth")
    public ResponseEntity<byte[]> login(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {
        return forward(authUrl, HttpMethod.POST, "/auth", request, body);
    }

    @GetMapping("/hello")
    public ResponseEntity<byte[]> hello(HttpServletRequest request) {
        return forward(authUrl, HttpMethod.GET, "/hello", request, null);
    }

    @GetMapping("/products")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return forward(productUrl, HttpMethod.GET, "/products", request, null);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<byte[]> findById(
            @PathVariable Integer id,
            HttpServletRequest request) {
        return forward(productUrl, HttpMethod.GET, "/products/" + id, request, null);
    }

    @PostMapping("/products")
    public ResponseEntity<byte[]> create(
            HttpServletRequest request,
            @RequestBody byte[] body) {
        return forward(productUrl, HttpMethod.POST, "/products", request, body);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<byte[]> update(
            @PathVariable Integer id,
            HttpServletRequest request,
            @RequestBody byte[] body) {
        return forward(productUrl, HttpMethod.PUT, "/products/" + id, request, body);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<byte[]> delete(
            @PathVariable Integer id,
            HttpServletRequest request) {
        return forward(productUrl, HttpMethod.DELETE, "/products/" + id, request, null);
    }

    @GetMapping("/orders")
    public ResponseEntity<byte[]> listOrders(HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.GET, "/orders", request, null);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<byte[]> findOrderById(
            @PathVariable Integer id, HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.GET, "/orders/" + id, request, null);
    }

    @PostMapping("/orders")
    public ResponseEntity<byte[]> createOrder(
            HttpServletRequest request, @RequestBody byte[] body) {
        return forward(orderUrl, HttpMethod.POST, "/orders", request, body);
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<byte[]> updateOrder(
            @PathVariable Integer id, HttpServletRequest request, @RequestBody byte[] body) {
        return forward(orderUrl, HttpMethod.PUT, "/orders/" + id, request, body);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<byte[]> deleteOrder(
            @PathVariable Integer id, HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.DELETE, "/orders/" + id, request, null);
    }

    @GetMapping("/order_items")
    public ResponseEntity<byte[]> listOrderItems(HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.GET, "/order_items", request, null);
    }

    @GetMapping("/order_items/{id}")
    public ResponseEntity<byte[]> findOrderItemById(
            @PathVariable Integer id, HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.GET, "/order_items/" + id, request, null);
    }

    @PostMapping("/order_items")
    public ResponseEntity<byte[]> createOrderItem(
            HttpServletRequest request, @RequestBody byte[] body) {
        return forward(orderUrl, HttpMethod.POST, "/order_items", request, body);
    }

    @PutMapping("/order_items/{id}")
    public ResponseEntity<byte[]> updateOrderItem(
            @PathVariable Integer id, HttpServletRequest request, @RequestBody byte[] body) {
        return forward(orderUrl, HttpMethod.PUT, "/order_items/" + id, request, body);
    }

    @DeleteMapping("/order_items/{id}")
    public ResponseEntity<byte[]> deleteOrderItem(
            @PathVariable Integer id, HttpServletRequest request) {
        return forward(orderUrl, HttpMethod.DELETE, "/order_items/" + id, request, null);
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<byte[]> getSummaryReport(HttpServletRequest request) {
        return forward(reportingUrl, HttpMethod.GET, "/api/v1/reports/summary", request, null);
    }

    @GetMapping("/reports/products")
    public ResponseEntity<byte[]> getProductReports(HttpServletRequest request) {
        return forward(reportingUrl, HttpMethod.GET, "/api/v1/reports/products", request, null);
    }

    @GetMapping("/reports/orders")
    public ResponseEntity<byte[]> getOrderReports(HttpServletRequest request) {
        return forward(reportingUrl, HttpMethod.GET, "/api/v1/reports/orders", request, null);
    }

    private ResponseEntity<byte[]> forward(
            String serviceUrl,
            HttpMethod method,
            String path,
            HttpServletRequest request,
            byte[] body) {
        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(URI.create(serviceUrl + path))
                .headers(headers -> copyRequestHeaders(request, headers));
        if (body != null) {
            requestSpec.body(body);
        }
        return requestSpec.exchange((clientRequest, clientResponse) -> {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    if (contentType != null) {
                        responseHeaders.setContentType(contentType);
                    }
                    List<String> locations = clientResponse.getHeaders().get(HttpHeaders.LOCATION);
                    if (locations != null) {
                        responseHeaders.put(HttpHeaders.LOCATION, locations);
                    }
                    return ResponseEntity.status(clientResponse.getStatusCode())
                            .headers(responseHeaders)
                            .body(clientResponse.getBody().readAllBytes());
                });
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpHeaders headers) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        String contentType = request.getContentType();
        if (contentType != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        }
    }
}
