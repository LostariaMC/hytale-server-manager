package fr.lostaria.hytaleservermanager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "hytale_auth_token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HytaleAuthToken {

    @Id
    private String id;

    @Column(nullable = false, length = 4096)
    private String refreshToken;

    @Column(nullable = false)
    private Instant expiresAt;
}
