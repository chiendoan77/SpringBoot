package com.example.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "jwt.secret=test-signing-key-that-is-at-least-32-bytes-long",
        "auth.username=admin",
        "auth.password=123"
})
@AutoConfigureMockMvc
class AuthServiceSecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginIssuesJwtAndRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth")
                        .param("userName", "admin")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/auth")
                        .param("userName", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void protectedAuthServiceEndpointRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isUnauthorized());
    }
}
