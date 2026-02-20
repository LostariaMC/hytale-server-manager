package fr.lostaria.hytaleservermanager.services.hytale;

import fr.lostaria.hytaleservermanager.entities.HytaleAuthToken;
import fr.lostaria.hytaleservermanager.payload.hytale.HytaleDeviceAuthResponse;
import fr.lostaria.hytaleservermanager.payload.hytale.OAuthTokenResponse;
import fr.lostaria.hytaleservermanager.repositories.HytaleAuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class HytaleOAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HytaleOAuthService.class);

    private static final String OAUTH_BASE = "https://oauth.accounts.hytale.com";
    private static final String CLIENT_ID = "hytale-server";
    private static final String SCOPE = "openid offline auth:server";
    private static final String PRIMARY_ID = "primary";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HytaleAuthTokenRepository tokenRepo;

    public HytaleOAuthService(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            HytaleAuthTokenRepository tokenRepo
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.tokenRepo = tokenRepo;
    }

    public boolean hasValidRefreshToken() {
        return tokenRepo.findById(PRIMARY_ID)
                .filter(t -> t.getRefreshToken() != null && !t.getRefreshToken().isBlank())
                .filter(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public void storeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is blank");
        }

        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        HytaleAuthToken token = HytaleAuthToken.builder()
                .id(PRIMARY_ID)
                .refreshToken(refreshToken.trim())
                .expiresAt(expiresAt)
                .build();

        tokenRepo.save(token);
        LOGGER.info("Hytale refresh_token stored in DB (id={}, expiresAt={})", PRIMARY_ID, expiresAt);
    }

    public HytaleDeviceAuthResponse requestDeviceCode() {
        try {
            URI uri = URI.create(OAUTH_BASE + "/oauth2/device/auth");

            String form = "client_id=" + enc(CLIENT_ID)
                    + "&scope=" + enc(SCOPE);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("device/auth failed: status=" + res.statusCode() + " body=" + res.body());
            }

            return objectMapper.readValue(res.body(), HytaleDeviceAuthResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Device code request failed: " + e.getMessage(), e);
        }
    }

    public OAuthTokenResponse pollDeviceCode(String deviceCode) {
        try {
            URI uri = URI.create(OAUTH_BASE + "/oauth2/token");

            String form = "client_id=" + enc(CLIENT_ID)
                    + "&grant_type=" + enc("urn:ietf:params:oauth:grant-type:device_code")
                    + "&device_code=" + enc(deviceCode);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("poll token failed: status=" + res.statusCode() + " body=" + res.body());
            }

            return objectMapper.readValue(res.body(), OAuthTokenResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Device code poll failed: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String requestAccessToken() {
        HytaleAuthToken row = tokenRepo.findPrimaryForUpdate().orElse(null);

        if (row == null || row.getRefreshToken() == null || row.getRefreshToken().isBlank()) {
            throw new IllegalStateException("Hytale refresh_token missing. Wait for startup auth to complete.");
        }
        if (row.getExpiresAt() == null || row.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Hytale refresh_token expired (local). Re-auth required.");
        }

        try {
            URI uri = URI.create(OAUTH_BASE + "/oauth2/token");

            String form = "client_id=" + enc(CLIENT_ID)
                    + "&grant_type=" + enc("refresh_token")
                    + "&refresh_token=" + enc(row.getRefreshToken());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();

            if (res.statusCode() / 100 != 2) {
                OAuthTokenResponse err = tryParseOAuthError(body);

                if (err != null && "invalid_grant".equals(err.error())) {
                    tokenRepo.deleteById(PRIMARY_ID);
                    throw new IllegalStateException("Hytale refresh_token invalid_grant (expired/revoked/already used). Re-auth required (check logs).");
                }

                throw new IllegalStateException("OAuth refresh failed: status=" + res.statusCode() + " body=" + body);
            }

            OAuthTokenResponse token = objectMapper.readValue(body, OAuthTokenResponse.class);

            if (token.accessToken() == null || token.accessToken().isBlank()) {
                throw new IllegalStateException("OAuth response missing access_token");
            }

            if (token.refreshToken() != null && !token.refreshToken().isBlank()) {
                row.setRefreshToken(token.refreshToken().trim());
                row.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
                tokenRepo.save(row);
            }

            return token.accessToken();

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("OAuth refresh failed: " + e.getMessage(), e);
        }
    }

    private OAuthTokenResponse tryParseOAuthError(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readValue(body, OAuthTokenResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
