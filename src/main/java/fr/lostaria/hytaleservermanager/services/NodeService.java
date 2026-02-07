package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.models.RegisterNodeModel;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
        node.setPortRangeStart(registerNodeModel.getPortRangeStart());
        node.setPortRangeEnd(registerNodeModel.getPortRangeEnd());
        node.setNextPort(registerNodeModel.getPortRangeStart());
        return nodeRepository.save(node);
    }

    public Node getLeastLoadedNode() {
        return nodeRepository
                .findLeastLoadedNodes(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

}
