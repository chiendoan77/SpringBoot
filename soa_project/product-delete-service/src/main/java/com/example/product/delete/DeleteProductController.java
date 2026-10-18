package com.example.product.delete;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class DeleteProductController {
    private final ProductDataClient productDataClient;

    public DeleteProductController(ProductDataClient productDataClient) {
        this.productDataClient = productDataClient;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<byte[]> delete(
            @PathVariable Integer id, HttpServletRequest request) {
        return productDataClient.forward(
                HttpMethod.DELETE, "/products/" + id, request, null);
    }
}
