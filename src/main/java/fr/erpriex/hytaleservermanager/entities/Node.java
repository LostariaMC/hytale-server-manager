package fr.erpriex.hytaleservermanager.entities;

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

}
