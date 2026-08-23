package com.lending.platform.service.impl;

import com.lending.platform.dto.request.LoginRequest;
import com.lending.platform.dto.response.LoginResponse;
import com.lending.platform.entity.Role;
import com.lending.platform.entity.User;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                jwtService
        );
    }

    @Test
    void login_shouldReturnLoginResponseWhenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(
                "admin@lending.local",
                "Admin123!"
        );

        Role role = new Role();
        role.setName("ADMIN");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");
        user.setPasswordHash("hashed-password");
        user.setRoles(Set.of(role));

        when(userRepository.findByEmailIgnoreCase(request.email()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1L, response.userId());
        assertEquals("System", response.firstName());
        assertEquals("Administrator", response.lastName());
        assertEquals("admin@lending.local", response.email());
        assertTrue(response.roles().contains("ADMIN"));

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));

        verify(userRepository)
                .findByEmailIgnoreCase("admin@lending.local");

        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldThrowBadCredentialsWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest(
                "admin@lending.local",
                "WrongPassword!"
        );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new BadCredentialsException(
                "Bad credentials"
        ));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());

        verify(jwtService, never())
                .generateToken(any());
    }
}
