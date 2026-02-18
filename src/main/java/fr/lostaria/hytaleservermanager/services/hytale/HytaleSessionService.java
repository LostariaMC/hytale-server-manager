package fr.lostaria.hytaleservermanager.services.hytale;

import fr.lostaria.hytaleservermanager.payload.hytale.GameSessionResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class HytaleSessionService {

    private static final String SESSIONS_BASE = "https://sessions.hytale.com";

    private final HytaleOAuthService oauth;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HytaleSessionService(
            HytaleOAuthService oauth,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.oauth = oauth;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public GameSessionResponse createGameSession(String profileUuid) {
        try {
            if (profileUuid == null || profileUuid.isBlank()) {
                throw new IllegalArgumentException("profileUuid is blank");
            }

            String accessToken = oauth.requestAccessToken();
            URI uri = URI.create(SESSIONS_BASE + "/game-session/new");

            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("uuid", profileUuid);
            String body = objectMapper.writeValueAsString(bodyNode);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("game-session/new failed: " + res.statusCode() + " body=" + res.body());
            }

            GameSessionResponse gs = objectMapper.readValue(res.body(), GameSessionResponse.class);

            if (gs.sessionToken() == null || gs.sessionToken().isBlank()
                    || gs.identityToken() == null || gs.identityToken().isBlank()) {
                throw new IllegalStateException("game-session/new response missing tokens");
            }

            return gs;
        } catch (Exception e) {
            throw new IllegalStateException("Create game session failed: " + e.getMessage(), e);
        }
    }
}
