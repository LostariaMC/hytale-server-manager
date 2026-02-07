package fr.lostaria.hytaleservermanager.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "nodes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String ip;

    @Column(nullable = false)
    private int portRangeStart;

    @Column(nullable = false)
    private int portRangeEnd;

    @Column(nullable = false)
    private int nextPort;

    @OneToMany(mappedBy = "node", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Server> servers = new ArrayList<>();

}
