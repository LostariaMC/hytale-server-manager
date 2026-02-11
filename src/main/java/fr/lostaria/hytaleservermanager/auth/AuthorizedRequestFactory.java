package fr.lostaria.hytaleservermanager.auth;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

@Component
public class AuthorizedRequestFactory {

    private final AuthTokenService authTokenService;

    public AuthorizedRequestFactory(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    public HttpRequest.Builder builder(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + authTokenService.getJwt());
    }
}
