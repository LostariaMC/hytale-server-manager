package fr.lostaria.hytaleservermanager.repositories;

import fr.lostaria.hytaleservermanager.entities.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRepository extends JpaRepository<Node, String> {
    boolean existsByPublicIp(String publicIp);
}
