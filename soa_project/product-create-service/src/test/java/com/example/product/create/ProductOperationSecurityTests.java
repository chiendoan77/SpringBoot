package com.example.product.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class ProductOperationSecurityTests {
    private static final String JWT_SECRET = "test-signing-key-that-is-at-least-32-bytes-long";
    private static final String PRODUCT_JSON =
            "{\"name\":\"Keyboard\",\"description\":\"Mechanical\",\"price\":79.99,\"quantity\":12}";

    @Test
    void createServiceRequiresAndForwardsValidJwt() throws Exception {
        AtomicReference<String> request = new AtomicReference<>();
        HttpServer dataServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        dataServer.createContext("/internal/products", exchange -> {
            request.set(exchange.getRequestMethod() + " "
                    + exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            byte[] response = PRODUCT_JSON.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        dataServer.start();

        try (var context = new SpringApplicationBuilder(ProductCreateApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--server.port=0",
                        "--jwt.secret=" + JWT_SECRET,
                        "--product-data.url=http://127.0.0.1:" + dataServer.getAddress().getPort())) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            RestClient client = RestClient.create();

            ResponseEntity<String> missingToken = client.post()
                    .uri("http://127.0.0.1:" + port + "/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(PRODUCT_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 401, (httpRequest, httpResponse) -> {
                    })
                    .toEntity(String.class);
            assertEquals(401, missingToken.getStatusCode().value());
            assertEquals(null, request.get());

            String token = Jwts.builder()
                    .subject("admin")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60_000))
                    .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .compact();
            ResponseEntity<String> response = client.post()
                    .uri("http://127.0.0.1:" + port + "/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(PRODUCT_JSON)
                    .retrieve()
                    .toEntity(String.class);

            assertEquals(201, response.getStatusCode().value());
            assertTrue(response.getBody().contains("Keyboard"));
            assertEquals("POST Bearer " + token, request.get());
        } finally {
            dataServer.stop(0);
        }
    }
}
