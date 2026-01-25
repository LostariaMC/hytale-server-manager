package fr.lostaria.hytaleservermanager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    public String publicIp;

}
