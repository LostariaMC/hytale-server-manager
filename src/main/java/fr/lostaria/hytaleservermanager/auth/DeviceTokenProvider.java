package fr.lostaria.hytaleservermanager.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DeviceTokenProvider {

    private final AuthProperties props;

    public DeviceTokenProvider(AuthProperties props) {
        this.props = props;
    }

    public String readDeviceToken() {
        Path p = Path.of(props.deviceTokenPath());
        try {
            if (!Files.exists(p)) {
                throw new IllegalStateException("deviceToken file not found: " + p.toAbsolutePath());
            }
            String s = Files.readString(p, StandardCharsets.UTF_8).trim();
            if (s.isBlank()) {
                throw new IllegalStateException("deviceToken file is empty: " + p.toAbsolutePath());
            }
            return s;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read deviceToken file: " + p.toAbsolutePath(), e);
        }
    }
}
