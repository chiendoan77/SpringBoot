package com.example.order.client;

import java.math.BigDecimal;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class ProductClient {
    private final RestClient restClient = RestClient.builder().build();
    private final String productServiceUrl;

    public ProductClient(@Value("${product-service.url:http://localhost:8086}") String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
    }

    public Product findById(Integer productId, String authorization) {
        try {
            Product product = restClient.get()
                    .uri(URI.create(productServiceUrl + "/products/" + productId))
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(Product.class);
            if (product == null
                    || product.id() == null
                    || !productId.equals(product.id())
                    || product.name() == null
                    || product.price() == null
                    || product.quantity() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service returned an invalid response");
            }
            return product;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found", exception);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Product service returned HTTP " + exception.getStatusCode().value(),
                    exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service is unavailable", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service returned an invalid response", exception);
        }
    }

    public record Product(Integer id, String name, BigDecimal price, Integer quantity) {
    }
}
