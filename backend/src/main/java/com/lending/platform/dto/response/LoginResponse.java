package com.lending.platform.dto.response;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String firstName,
        String lastName,
        String email,
        Set<String> roles
) {
}
