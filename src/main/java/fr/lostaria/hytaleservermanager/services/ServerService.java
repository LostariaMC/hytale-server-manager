package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import org.springframework.stereotype.Service;

@Service
public class ServerService {

    private final NodeRepository nodeRepository;
    private final MessageService messageService;

    public ServerService(NodeRepository nodeRepository, MessageService messageService) {
        this.nodeRepository = nodeRepository;
        this.messageService = messageService;
    }

    public void startServer() {
        Node node = nodeRepository.findAll().get(0);
        messageService.send(node.getId(), "Hello world !");
    }

}
