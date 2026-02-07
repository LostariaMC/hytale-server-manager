package fr.lostaria.hytaleservermanager.mappers;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.models.NodeModel;
import org.springframework.stereotype.Component;

@Component
public class NodeMapper {

    public NodeModel toModel(Node node) {
        NodeModel dto = new NodeModel();
        dto.setId(node.getId());
        dto.setIp(node.getIp());
        dto.setPortRangeStart(node.getPortRangeStart());
        dto.setPortRangeEnd(node.getPortRangeEnd());
        return dto;
    }

}
