package fr.erpriex.hytaleservermanager.repositories;

import fr.erpriex.hytaleservermanager.entities.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Node, String> {
}
