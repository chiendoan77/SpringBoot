package com.example.product.detail;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class GetProductController {
    private final ProductDataClient productDataClient;

    public GetProductController(ProductDataClient productDataClient) {
        this.productDataClient = productDataClient;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findById(
            @PathVariable Integer id, HttpServletRequest request) {
        return productDataClient.forward(
                HttpMethod.GET, "/products/" + id, request);
    }
}
