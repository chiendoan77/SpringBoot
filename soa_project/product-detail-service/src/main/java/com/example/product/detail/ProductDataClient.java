package com.example.product.detail;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductDataClient {
    private final RestClient restClient = RestClient.builder().build();
    private final String productDataUrl;

    public ProductDataClient(
            @Value("${product-data.url:http://localhost:8087}") String productDataUrl) {
        this.productDataUrl = productDataUrl;
    }

    public ResponseEntity<byte[]> forward(
            HttpMethod method, String path, HttpServletRequest request) {
        return restClient.method(method)
                .uri(URI.create(productDataUrl + "/internal" + path))
                .headers(headers -> {
                    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                    if (authorization != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                })
                .exchange((clientRequest, clientResponse) -> {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    if (contentType != null) {
                        responseHeaders.setContentType(contentType);
                    }
                    return ResponseEntity.status(clientResponse.getStatusCode())
                            .headers(responseHeaders)
                            .body(clientResponse.getBody().readAllBytes());
                });
    }
}
