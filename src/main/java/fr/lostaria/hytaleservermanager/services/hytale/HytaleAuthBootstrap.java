package fr.lostaria.hytaleservermanager.services.hytale;

import fr.lostaria.hytaleservermanager.payload.hytale.HytaleDeviceAuthResponse;
import fr.lostaria.hytaleservermanager.payload.hytale.OAuthTokenResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HytaleAuthBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(HytaleAuthBootstrap.class);

    private final HytaleOAuthService oauth;

    private volatile boolean pollingActive = false;
    private volatile String deviceCode = null;
    private volatile Instant deviceCodeExpiresAt = null;
    private volatile long lastLinkLogMillis = 0L;

    public HytaleAuthBootstrap(HytaleOAuthService oauth) {
        this.oauth = oauth;
    }

    @PostConstruct
    public void init() {
        if (!oauth.hasValidRefreshToken()) {
            startDeviceFlow();
        } else {
            LOGGER.info("Hytale refresh_token OK at startup.");
        }
    }

    @Scheduled(fixedDelay = 10_000L)
    public void ensureAuthIfMissing() {
        if (!pollingActive && !oauth.hasValidRefreshToken()) {
            startDeviceFlow();
        }
    }

    @Scheduled(fixedDelay = 5_000L)
    public void pollIfActive() {
        if (!pollingActive) return;

        if (oauth.hasValidRefreshToken()) {
            stopPolling("Refresh token became valid.");
            return;
        }

        if (deviceCodeExpiresAt != null && Instant.now().isAfter(deviceCodeExpiresAt)) {
            LOGGER.warn("Hytale device_code expired. Requesting a new one...");
            startDeviceFlow();
            return;
        }

        if (deviceCode == null || deviceCode.isBlank()) {
            startDeviceFlow();
            return;
        }

        try {
            OAuthTokenResponse res = oauth.pollDeviceCode(deviceCode);

            if (res.error() != null && !res.error().isBlank()) {
                return;
            }

            if (res.refreshToken() == null || res.refreshToken().isBlank()) {
                return;
            }

            oauth.storeRefreshToken(res.refreshToken());
            stopPolling("Device auth validated. Refresh token stored.");

        } catch (Exception e) {
            LOGGER.warn("Hytale poll failed: {}", e.toString());
        }
    }

    private void startDeviceFlow() {
        try {
            HytaleDeviceAuthResponse dc = oauth.requestDeviceCode();

            this.deviceCode = dc.deviceCode();
            this.deviceCodeExpiresAt = Instant.now().plusSeconds(Math.max(1, dc.expiresIn()));
            this.pollingActive = true;

            long now = System.currentTimeMillis();
            if (now - lastLinkLogMillis > 5_000L) {
                lastLinkLogMillis = now;
                LOGGER.warn("==== HYTALE AUTH REQUIRED ====");
                LOGGER.warn("Open this URL and login: {}", dc.verificationUriComplete());
                LOGGER.warn("User code (if needed): {}", dc.userCode());
                LOGGER.warn("This link expires in ~{} seconds.", dc.expiresIn());
                LOGGER.warn("After validation, the API will automatically store the refresh token.");
                LOGGER.warn("==============================");
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to start Hytale device flow: {}", e.toString());
            this.pollingActive = false;
        }
    }

    private void stopPolling(String reason) {
        LOGGER.info("Stopping Hytale device polling: {}", reason);
        this.pollingActive = false;
        this.deviceCode = null;
        this.deviceCodeExpiresAt = null;
    }
}
