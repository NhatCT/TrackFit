package com.ntn.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    @Test
    void generateAndValidate_returnsUsername() throws Exception {
        String token = JwtUtils.generateToken("testuser", List.of("USER"));
        String username = JwtUtils.validateTokenAndGetUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void generateToken_withRoles_rolesExtracted() throws Exception {
        List<String> roles = List.of("ADMIN", "USER");
        String token = JwtUtils.generateToken("admin", roles);

        List<String> extracted = JwtUtils.getRoles(token);
        assertEquals(2, extracted.size());
        assertTrue(extracted.contains("ADMIN"));
        assertTrue(extracted.contains("USER"));
    }

    @Test
    void generateToken_noRoles_emptyRolesList() throws Exception {
        String token = JwtUtils.generateToken("user1");
        List<String> roles = JwtUtils.getRoles(token);
        assertTrue(roles.isEmpty());
    }

    @Test
    void validateToken_invalidSignature_returnsNull() throws Exception {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid";
        String result = JwtUtils.validateTokenAndGetUsername(fakeToken);
        assertNull(result);
    }

    @Test
    void generateToken_differentUsers_differentTokens() throws Exception {
        String t1 = JwtUtils.generateToken("user1");
        String t2 = JwtUtils.generateToken("user2");
        assertNotEquals(t1, t2);
    }

    @Test
    void getRoles_withNullRoles_returnsEmptyList() throws Exception {
        String token = JwtUtils.generateToken("user", null);
        List<String> roles = JwtUtils.getRoles(token);
        assertTrue(roles.isEmpty());
    }
}
