package com.lending.platform.security;

import com.lending.platform.entity.Role;
import com.lending.platform.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {

        // Base64-encoded 256-bit test key.
        String testSecret =
                "VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS10aGF0LWlzLWxvbmc=";

        long expirationMs = 3_600_000;

        jwtService = new JwtService(
                testSecret,
                expirationMs
        );

        Role role = new Role();
        role.setName("ADMIN");

        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");
        user.setRoles(Set.of(role));
    }

    @Test
    void generateToken_shouldCreateToken() {

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_shouldReturnUserEmail() {

        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals(
                "admin@lending.local",
                email
        );
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {

        boolean valid = jwtService.isTokenValid(
                "this.is.not.a.valid.jwt"
        );

        assertFalse(valid);
    }
}