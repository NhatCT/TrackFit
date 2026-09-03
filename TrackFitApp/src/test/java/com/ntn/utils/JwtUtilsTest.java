package com.ntn.utils;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilsTest {

    @Test
    void generatedTokenContainsTheExpectedSubjectAndRoles() throws Exception {
        String token = JwtUtils.generateToken("alice", List.of("ROLE_USER"));

        assertEquals("alice", JwtUtils.validateTokenAndGetUsername(token));
        assertEquals(List.of("ROLE_USER"), JwtUtils.getRoles(token));
    }
}
