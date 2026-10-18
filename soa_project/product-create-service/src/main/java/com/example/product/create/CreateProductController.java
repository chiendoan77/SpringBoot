package com.example.product.create;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class CreateProductController {
    private final ProductDataClient productDataClient;

    public CreateProductController(ProductDataClient productDataClient) {
        this.productDataClient = productDataClient;
    }

    @PostMapping
    public ResponseEntity<byte[]> create(
            HttpServletRequest request,
            @RequestBody byte[] body) {
        return productDataClient.forward(HttpMethod.POST, "/products", request, body);
    }
}
