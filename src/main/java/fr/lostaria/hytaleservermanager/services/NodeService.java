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
        Node node = Node.builder()
                .id(UUID.randomUUID().toString())
                .ip(registerNodeModel.getIp())
                .portRangeStart(registerNodeModel.getPortRangeStart())
                .portRangeEnd(registerNodeModel.getPortRangeEnd())
                .nextPort(registerNodeModel.getPortRangeStart())
                .build();

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
