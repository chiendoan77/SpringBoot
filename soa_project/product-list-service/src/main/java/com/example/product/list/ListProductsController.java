package com.example.product.list;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ListProductsController {
    private final ProductDataClient productDataClient;

    public ListProductsController(ProductDataClient productDataClient) {
        this.productDataClient = productDataClient;
    }

    @GetMapping
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return productDataClient.forward(HttpMethod.GET, "/products", request);
    }
}
