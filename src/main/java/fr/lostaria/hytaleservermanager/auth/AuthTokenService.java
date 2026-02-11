package fr.lostaria.hytaleservermanager.auth;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class AuthTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenService.class);

    private final AuthProperties props;
    private final DeviceTokenProvider deviceTokenProvider;
    private final HttpClient httpClient;

    private volatile String cachedJwt;

    public AuthTokenService(
            AuthProperties props,
            DeviceTokenProvider deviceTokenProvider,
            HttpClient httpClient
    ) {
        this.props = props;
        this.deviceTokenProvider = deviceTokenProvider;
        this.httpClient = httpClient;
    }

    @PostConstruct
    public void init() {
        try {
            refreshJwtOrThrow();
            LOGGER.info("JWT fetched at startup.");
        } catch (Exception e) {
            LOGGER.warn("JWT fetch at startup failed: {}", e.toString());
        }
    }

    @Scheduled(fixedRate = 55 * 60 * 1000L, initialDelay = 55 * 60 * 1000L)
    public void scheduledRefresh() {
        try {
            refreshJwtOrThrow();
            LOGGER.info("JWT refreshed.");
        } catch (Exception e) {
            LOGGER.warn("JWT refresh failed: {}", e.toString());
        }
    }

    public String getJwt() {
        String jwt = cachedJwt;
        if (jwt != null && !jwt.isBlank()) {
            return jwt;
        }

        synchronized (this) {
            jwt = cachedJwt;
            if (jwt != null && !jwt.isBlank()) {
                return jwt;
            }

            refreshJwtOrThrow();
            return cachedJwt;
        }
    }

    private void refreshJwtOrThrow() {
        this.cachedJwt = fetchJwtOrThrow();
    }

    private String fetchJwtOrThrow() {
        try {
            String baseUrl = stripTrailingSlash(props.baseUrl());
            String deviceToken = deviceTokenProvider.readDeviceToken();
            String encoded = URLEncoder.encode(deviceToken, StandardCharsets.UTF_8);

            URI uri = URI.create(baseUrl + "/token?deviceToken=" + encoded);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> res =
                    httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            int code = res.statusCode();
            if (code == 200 || code == 201) {
                String jwt = res.body() == null ? "" : res.body().trim();
                if (jwt.isBlank()) {
                    throw new IllegalStateException("Auth returned empty body");
                }
                return jwt;
            }

            throw new IllegalStateException(
                    "Auth failed: status=" + code + " body=" + res.body()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Auth token fetch failed: " + e.getMessage(), e
            );
        }
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
