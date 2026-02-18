package fr.lostaria.hytaleservermanager.payload.hytale;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("scope") String scope,
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription
) {}
