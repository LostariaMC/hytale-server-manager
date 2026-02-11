package fr.lostaria.hytaleservermanager.pubsub;

import fr.lostaria.hytaleservermanager.auth.AuthorizedRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class PubsubClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final AuthorizedRequestFactory requestFactory;
    private final ObjectMapper objectMapper;

    public PubsubClient(
            @Value("${pubsub.base-url}") String baseUrl,
            HttpClient httpClient,
            AuthorizedRequestFactory requestFactory,
            ObjectMapper objectMapper
    ) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.httpClient = httpClient;
        this.requestFactory = requestFactory;
        this.objectMapper = objectMapper;
    }

    public void send(String consumer, String type, JsonNode payload) {
        try {
            URI uri = URI.create(baseUrl + "/messages/" + consumer);

            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("type", type);
            envelope.set("payload", payload);

            String body = objectMapper.writeValueAsString(envelope);

            HttpRequest request = requestFactory.builder(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException(
                        "Pubsub send failed: status=" + code + " body=" + response.body()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Pubsub send failed", e);
        }
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
