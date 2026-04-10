package com.jaycong.boot.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.modules.auth.entity.LoginLogEntity;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.LoginLogMapper;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void register_shouldReturnTokenAndUser() throws Exception {
        String body = """
                {
                  \"email\": \"new.user@example.com\",
                  \"password\": \"Password123\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("new.user@example.com"));
    }

    @Test
    void register_shouldPersistUser() throws Exception {
        register("tenant.owner@example.com", "Password123");

        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, "tenant.owner@example.com")
                .last("limit 1"));
        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertNotNull(user.getUsername());
    }

    @Test
    void register_duplicateEmail_shouldReturnConflict() throws Exception {
        String body = """
                {
                  "email": "duplicate.user@example.com",
                  "password": "Password123"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void loginAndSession_shouldSucceed() throws Exception {
        String email = "session.user@example.com";
        register(email, "Password123");

        MvcResult loginResult = login(email, "Password123");
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/session")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.loginId").isNumber());
    }

    @Test
    void changePassword_shouldRejectOldAndAcceptNewPassword() throws Exception {
        String email = "change.password@example.com";
        register(email, "Password123");

        MvcResult loginResult = login(email, "Password123");
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("token")
                .asText();

        String changeBody = """
                {
                  "oldPassword": "Password123",
                  "newPassword": "Password456"
                }
                """;
        mockMvc.perform(post("/api/auth/password/change")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "change.password@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        login(email, "Password456");
    }

    @Test
    void refreshToken_shouldSucceedWhenLoggedIn() throws Exception {
        String email = "refresh.user@example.com";
        register(email, "Password123");
        MvcResult loginResult = login(email, "Password123");
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("token")
                .asText();

        mockMvc.perform(post("/api/auth/token/refresh")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loginFailure_shouldWriteLoginLog() throws Exception {
        String email = "log.failed@example.com";
        register(email, "Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "log.failed@example.com",
                                  "password": "Password000"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        long failCount = loginLogMapper.selectCount(new LambdaQueryWrapper<LoginLogEntity>()
                .eq(LoginLogEntity::getReason, "PASSWORD_MISMATCH")
                .eq(LoginLogEntity::getSuccess, false));
        org.junit.jupiter.api.Assertions.assertTrue(failCount > 0);
    }

    @Test
    void session_withoutLogin_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldInvalidateSession() throws Exception {
        String email = "logout.user@example.com";
        register(email, "Password123");
        MvcResult loginResult = login(email, "Password123");
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("token")
                .asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/auth/session")
                        .header("satoken", token))
                .andExpect(status().isUnauthorized());
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
    }
}
