package fr.lostaria.hytaleservermanager.entities;

import fr.lostaria.hytaleservermanager.payload.ServerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Node node;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    private ServerStatus status;

}
