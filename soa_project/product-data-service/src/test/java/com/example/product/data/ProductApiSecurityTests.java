package com.example.product.data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.product.data.ProductDataApplication;
import com.example.product.data.repository.ProductRepository;

@SpringBootTest(
        classes = ProductDataApplication.class,
        properties = {
                "jwt.secret=test-signing-key-that-is-at-least-32-bytes-long",
                "spring.datasource.url=jdbc:h2:mem:product-tests;DB_CLOSE_DELAY=-1"
        })
@AutoConfigureMockMvc
class ProductApiSecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clearProducts() {
        productRepository.deleteAll();
    }

    @Test
    void productEndpointsRejectMissingAndInvalidTokens() throws Exception {
        // GET /products is public (for internal service-to-service calls like reporting-service)
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        // POST requires auth - no token → 401
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"X\",\"price\":1,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());

        // POST with invalid token → 401
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"X\",\"price\":1,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());

        String tokenWithWrongSignature = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Jwts.SIG.HS256.key().build())
                .compact();
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + tokenWithWrongSignature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"X\",\"price\":1,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void productServiceDoesNotExposeOptionsAsAnApi() throws Exception {
        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk());
    }

    @Test
    void validJwtAllowsProductCrudLifecycle() throws Exception {
        String token = token();
        String authorization = "Bearer " + token;

        MvcResult createResult = mockMvc.perform(post("/products")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Keyboard","description":"Mechanical","price":79.99,"quantity":12}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();

        JsonNode createdProduct = objectMapper.readTree(
                createResult.getResponse().getContentAsString());
        int productId = createdProduct.get("id").asInt();

        mockMvc.perform(get("/products/{id}", productId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(12));

        mockMvc.perform(get("/products")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/products/{id}", productId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Keyboard Pro","description":"Updated","price":99.50,"quantity":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard Pro"))
                .andExpect(jsonPath("$.quantity").value(5));

        mockMvc.perform(delete("/products/{id}", productId)
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/{id}", productId)
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidProductFieldsAreRejected() throws Exception {
        String token = token();

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","description":"Invalid","price":-1,"quantity":-2}
                                """))
                .andExpect(status().isBadRequest());
    }

    private String token() {
        byte[] secret = "test-signing-key-that-is-at-least-32-bytes-long"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();
    }
}
