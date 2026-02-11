package fr.lostaria.hytaleservermanager.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String baseUrl,
        String deviceTokenPath
) {}
