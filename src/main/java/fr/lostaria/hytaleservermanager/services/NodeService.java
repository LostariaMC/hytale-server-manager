package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.models.RegisterNodeModel;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public Node createNode(RegisterNodeModel registerNodeModel) {
        Node node = new Node();
        node.setId(UUID.randomUUID().toString());
        node.setIp(registerNodeModel.getIp());
        return nodeRepository.save(node);
    }

}
