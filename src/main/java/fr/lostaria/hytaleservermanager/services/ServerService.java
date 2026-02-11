package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.pubsub.PubsubClient;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ServerService {

    private final NodeRepository nodeRepository;
    private final PubsubClient pubsubClient;
    private final ObjectMapper objectMapper;

    public ServerService(
            NodeRepository nodeRepository,
            PubsubClient pubsubClient,
            ObjectMapper objectMapper
    ) {
        this.nodeRepository = nodeRepository;
        this.pubsubClient = pubsubClient;
        this.objectMapper = objectMapper;
    }

    public void createServer() {
        Node node = nodeRepository.findAll().get(0);

        JsonNode payload =
                objectMapper.getNodeFactory().textNode("Hello world");

        pubsubClient.send(
                "node-" + node.getId().toString(),
                "HELLO_WORLD",
                payload
        );
    }
}
