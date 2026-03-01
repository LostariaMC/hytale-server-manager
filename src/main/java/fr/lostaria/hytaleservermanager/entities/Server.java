package fr.lostaria.hytaleservermanager.entities;

import fr.lostaria.hytaleservermanager.payload.ServerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "servers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Node node;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private String image;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status;

}
