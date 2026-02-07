package fr.lostaria.hytaleservermanager.repositories;

import fr.lostaria.hytaleservermanager.entities.Node;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, String> {
    Optional<Node> findByIp(String ip);

    @Query("""
        SELECT n
        FROM Node n
        LEFT JOIN n.servers s
        GROUP BY n
        ORDER BY COUNT(s) ASC, n.id ASC
    """)
    List<Node> findLeastLoadedNodes(Pageable pageable);
}
