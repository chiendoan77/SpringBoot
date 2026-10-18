package com.example.product.update;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class UpdateProductController {
    private final ProductDataClient productDataClient;

    public UpdateProductController(ProductDataClient productDataClient) {
        this.productDataClient = productDataClient;
    }

    @PutMapping("/{id}")
    public ResponseEntity<byte[]> update(
            @PathVariable Integer id,
            HttpServletRequest request,
            @RequestBody byte[] body) {
        return productDataClient.forward(
                HttpMethod.PUT, "/products/" + id, request, body);
    }
}
