package fr.lostaria.hytaleservermanager.repositories;

import fr.lostaria.hytaleservermanager.entities.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, String> {
    @Query("select s.port from Server s where s.node.id = :nodeId")
    List<Integer> findAllPortsByNodeId(@Param("nodeId") String nodeId);
}
