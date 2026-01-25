package fr.lostaria.hytaleservermanager.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "servers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    @Id
    private String id;

}
