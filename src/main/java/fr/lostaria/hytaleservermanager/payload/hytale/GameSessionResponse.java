package fr.lostaria.hytaleservermanager.payload.hytale;

public record GameSessionResponse(
        String sessionToken,
        String identityToken,
        String expiresAt
) {}
