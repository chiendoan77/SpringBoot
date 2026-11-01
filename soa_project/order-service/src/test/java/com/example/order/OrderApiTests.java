package com.example.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class OrderApiTests {
    private static final String JWT_SECRET = "test-signing-key-that-is-at-least-32-bytes-long";
    private static final String PRODUCT_JSON =
            "{\"id\":1,\"name\":\"Keyboard\",\"price\":79.99,\"quantity\":10}";
    private static final String ORDER_JSON = """
            {
              "customerName": "Ada Lovelace",
              "customerEmail": "ada@example.com",
              "items": [{"productId": 1, "quantity": 2}]
            }
            """;

    @Test
    void jwtProtectedOrderAndItemEndpointsSupportCrudAndCheckStock() throws Exception {
        AtomicReference<String> productRequest = new AtomicReference<>();
        HttpServer productServer = fakeProductService(productRequest);
        String authorization = "Bearer " + token();

        try (var context = new SpringApplicationBuilder(OrderApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--server.port=0",
                        "--jwt.secret=" + JWT_SECRET,
                        "--spring.datasource.url=jdbc:h2:mem:order-tests;DB_CLOSE_DELAY=-1",
                        "--product-service.url=http://127.0.0.1:"
                                + productServer.getAddress().getPort())) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            String serviceUrl = "http://127.0.0.1:" + port;
            RestClient client = RestClient.create();
            ObjectMapper objectMapper = new ObjectMapper();

            assertEquals(401, client.get()
                    .uri(serviceUrl + "/orders")
                    .retrieve()
                    .onStatus(status -> status.value() == 401, (request, response) -> { })
                    .toEntity(String.class)
                    .getStatusCode()
                    .value());

            ResponseEntity<String> created = client.post()
                    .uri(serviceUrl + "/orders")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ORDER_JSON)
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(201, created.getStatusCode().value());
            JsonNode createdOrder = objectMapper.readTree(created.getBody());
            int orderId = createdOrder.get("id").asInt();
            assertEquals("pending", createdOrder.get("status").asText());
            assertEquals(159.98, createdOrder.get("totalAmount").asDouble());
            assertEquals(2, createdOrder.get("items").get(0).get("quantity").asInt());
            assertTrue(productRequest.get().contains(authorization));

            ResponseEntity<String> orders = client.get()
                    .uri(serviceUrl + "/orders")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(1, objectMapper.readTree(orders.getBody()).size());

            ResponseEntity<String> updatedOrder = client.put()
                    .uri(serviceUrl + "/orders/" + orderId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"COMPLETED\"}")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals("completed", objectMapper.readTree(updatedOrder.getBody())
                    .get("status").asText());

            ResponseEntity<String> createdItem = client.post()
                    .uri(serviceUrl + "/order_items")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"orderId\":" + orderId + ",\"productId\":1,\"quantity\":1}")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(201, createdItem.getStatusCode().value());
            int itemId = objectMapper.readTree(createdItem.getBody()).get("id").asInt();
            ResponseEntity<String> items = client.get()
                    .uri(serviceUrl + "/order_items")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(2, objectMapper.readTree(items.getBody()).size());
            assertEquals(itemId, objectMapper.readTree(client.get()
                    .uri(serviceUrl + "/order_items/" + itemId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody()).get("id").asInt());
            JsonNode orderAfterAddingItem = objectMapper.readTree(client.get()
                    .uri(serviceUrl + "/orders/" + orderId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody());
            assertEquals(239.97, orderAfterAddingItem.get("totalAmount").asDouble());

            ResponseEntity<String> updatedItem = client.put()
                    .uri(serviceUrl + "/order_items/" + itemId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"productId\":1,\"quantity\":2}")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(2, objectMapper.readTree(updatedItem.getBody()).get("quantity").asInt());

            assertEquals(409, client.post()
                    .uri(serviceUrl + "/orders")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ORDER_JSON.replace(
                            "\"items\": [{\"productId\": 1, \"quantity\": 2}]",
                            "\"items\": [{\"productId\": 1, \"quantity\": 6}, "
                                    + "{\"productId\": 1, \"quantity\": 5}]"))
                    .retrieve()
                    .onStatus(status -> status.value() == 409, (request, response) -> { })
                    .toEntity(String.class)
                    .getStatusCode()
                    .value());

            assertEquals(204, client.delete()
                    .uri(serviceUrl + "/order_items/" + itemId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .value());
            assertEquals(204, client.delete()
                    .uri(serviceUrl + "/orders/" + orderId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .value());
        } finally {
            productServer.stop(0);
        }
    }

    private HttpServer fakeProductService(AtomicReference<String> capturedRequest) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/products", exchange -> {
            capturedRequest.set(exchange.getRequestURI() + " "
                    + exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            byte[] response = PRODUCT_JSON.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }

    private String token() {
        return Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
