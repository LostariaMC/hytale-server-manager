package fr.lostaria.hytaleservermanager.entities;

import jakarta.persistence.*;
import lombok.*;

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

}
