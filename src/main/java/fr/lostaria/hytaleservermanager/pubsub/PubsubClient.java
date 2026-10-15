package fr.lostaria.hytaleservermanager.pubsub;

import fr.lostaria.hytaleservermanager.auth.AuthTokenService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Component
public class PubsubClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubsubClient.class);

    private static final String CONSUMER = "server-manager";

    private final URI wsUri;
    private final HttpClient httpClient;
    private final AuthTokenService authTokenService;
    private final ObjectMapper objectMapper;

    private WebSocket webSocket;

    public PubsubClient(
            @Value("${pubsub.base-url}") String baseUrl,
            HttpClient httpClient,
            AuthTokenService authTokenService,
            ObjectMapper objectMapper
    ) {
        this.wsUri = URI.create(toWebSocketUrl(stripTrailingSlash(baseUrl)) + "/ws/" + CONSUMER);
        this.httpClient = httpClient;
        this.authTokenService = authTokenService;
        this.objectMapper = objectMapper;
    }

    public synchronized void send(String consumer, String type, JsonNode payload) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("consumer", consumer);
        envelope.put("type", type);
        envelope.put("producer", CONSUMER);
        envelope.set("payload", payload);

        String body = objectMapper.writeValueAsString(envelope);

        try {
            sendText(body);
        } catch (Exception first) {
            LOGGER.warn("Pubsub send failed, reconnecting: {}", first.toString());
            disconnect();
            try {
                sendText(body);
            } catch (Exception e) {
                disconnect();
                throw new RuntimeException("Pubsub send failed", e);
            }
        }
    }

    @Scheduled(fixedRate = 30_000L, initialDelay = 30_000L)
    public synchronized void ping() {
        if (webSocket == null) return;
        try {
            webSocket.sendPing(ByteBuffer.allocate(0)).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Pubsub ping failed: {}", e.toString());
            disconnect();
        }
    }

    @PreDestroy
    public synchronized void close() {
        if (webSocket == null) return;
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown").get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        disconnect();
    }

    private void sendText(String body) throws Exception {
        connectIfNeeded().sendText(body, true).get(10, TimeUnit.SECONDS);
    }

    private WebSocket connectIfNeeded() throws Exception {
        if (webSocket != null && !webSocket.isOutputClosed() && !webSocket.isInputClosed()) {
            return webSocket;
        }

        webSocket = httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + authTokenService.getJwt())
                .buildAsync(wsUri, new Listener())
                .get(15, TimeUnit.SECONDS);

        LOGGER.info("Pubsub connected: {}", wsUri);
        return webSocket;
    }

    private void disconnect() {
        if (webSocket != null) {
            webSocket.abort();
            webSocket = null;
        }
    }

    private static final class Listener implements WebSocket.Listener {

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            LOGGER.warn("Ignored pubsub message: {}", data);
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            LOGGER.warn("Pubsub closed status={} reason={}", statusCode, reason);
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            LOGGER.warn("Pubsub error: {}", error.toString());
        }
    }

    private static String toWebSocketUrl(String url) {
        if (url.startsWith("https://")) return "wss://" + url.substring("https://".length());
        if (url.startsWith("http://")) return "ws://" + url.substring("http://".length());
        return url;
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
