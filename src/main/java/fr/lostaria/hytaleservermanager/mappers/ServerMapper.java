package fr.lostaria.hytaleservermanager.mappers;

import fr.lostaria.hytaleservermanager.entities.Server;
import fr.lostaria.hytaleservermanager.models.ServerModel;
import org.springframework.stereotype.Component;

@Component
public class ServerMapper {

    public ServerModel toModel(Server server) {
        ServerModel dto = new ServerModel();
        dto.setId(server.getId());
        dto.setNodeId(server.getNode().getId());
        dto.setPort(server.getPort());
        dto.setImage(server.getImage());
        return dto;
    }

}
