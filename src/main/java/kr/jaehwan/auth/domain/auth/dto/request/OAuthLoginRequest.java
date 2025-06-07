package kr.jaehwan.auth.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record OAuthLoginRequest(
        @NotNull
        String token
) {}