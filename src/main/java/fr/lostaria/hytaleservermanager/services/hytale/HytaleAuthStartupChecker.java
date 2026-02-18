package fr.lostaria.hytaleservermanager.services.hytale;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HytaleAuthStartupChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(HytaleAuthStartupChecker.class);

    private final HytaleOAuthService oauth;

    public HytaleAuthStartupChecker(HytaleOAuthService oauth) {
        this.oauth = oauth;
    }

    @PostConstruct
    public void check() {
        if (oauth.hasValidRefreshToken()) {
            LOGGER.info("Hytale refresh_token OK (DB present + expiresAt not passed).");
        } else {
            LOGGER.warn("Hytale refresh_token missing/expired. Server creation will fail until you renew it manually.");
        }
    }
}
