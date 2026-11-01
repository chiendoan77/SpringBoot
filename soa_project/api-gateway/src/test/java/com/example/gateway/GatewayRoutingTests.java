package com.example.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class GatewayRoutingTests {
    @Test
    void gatewayForwardsLoginAndBearerRequestsToConfiguredServices() throws Exception {
        AtomicReference<String> authRequest = new AtomicReference<>();
        AtomicReference<String> productRequest = new AtomicReference<>();
        AtomicReference<String> orderRequest = new AtomicReference<>();
        HttpServer authServer = fakeService(authRequest, 200, "{\"token\":\"signed-token\"}");
        HttpServer createServer = fakeService(productRequest, 201, "{\"name\":\"Keyboard\"}");
        HttpServer orderServer = fakeService(orderRequest, 200, "[{\"id\":1}]");

        try (var context = new SpringApplicationBuilder(ApiGatewayApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--server.port=0",
                        "--gateway.auth-url=http://127.0.0.1:" + authServer.getAddress().getPort(),
                        "--gateway.create-url=http://127.0.0.1:" + createServer.getAddress().getPort(),
                        "--gateway.delete-url=http://127.0.0.1:" + createServer.getAddress().getPort(),
                        "--gateway.update-url=http://127.0.0.1:" + createServer.getAddress().getPort(),
                        "--gateway.list-url=http://127.0.0.1:" + createServer.getAddress().getPort(),
                        "--gateway.detail-url=http://127.0.0.1:" + createServer.getAddress().getPort(),
                        "--gateway.order-url=http://127.0.0.1:" + orderServer.getAddress().getPort())) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            String gatewayUrl = "http://127.0.0.1:" + port;

            ResponseEntity<String> login = RestClient.create().post()
                    .uri(gatewayUrl + "/auth")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("userName=admin&password=123")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(200, login.getStatusCode().value());
            assertTrue(login.getBody().contains("signed-token"));
            assertTrue(authRequest.get().contains("POST /auth userName=admin&password=123"));

            ResponseEntity<String> create = RestClient.create().post()
                    .uri(gatewayUrl + "/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer sample.jwt.token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"name\":\"Keyboard\"}")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(201, create.getStatusCode().value());
            assertTrue(productRequest.get().contains("POST /products"));
            assertTrue(productRequest.get().contains("Bearer sample.jwt.token"));
            ResponseEntity<String> orders = RestClient.create().get()
                    .uri(gatewayUrl + "/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                    .retrieve()
                    .toEntity(String.class);
            assertEquals(200, orders.getStatusCode().value());
            assertTrue(orderRequest.get().contains("GET /orders"));
            assertTrue(orderRequest.get().contains("Bearer test-token"));
        } finally {
            authServer.stop(0);
            createServer.stop(0);
            orderServer.stop(0);
        }
    }

    private HttpServer fakeService(
            AtomicReference<String> capturedRequest,
            int status,
            String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            capturedRequest.set(exchange.getRequestMethod() + " " + exchange.getRequestURI()
                    + " " + body + " "
                    + exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }
}
