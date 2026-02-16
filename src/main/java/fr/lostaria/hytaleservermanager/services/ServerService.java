package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.pubsub.PubsubClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class ServerService {

    private final NodeService nodeService;
    private final PubsubClient pubsubClient;
    private final ObjectMapper objectMapper;

    public ServerService(
            NodeService nodeService,
            PubsubClient pubsubClient,
            ObjectMapper objectMapper
    ) {
        this.nodeService = nodeService;
        this.pubsubClient = pubsubClient;
        this.objectMapper = objectMapper;
    }

    public void createServer(String image) {
        Node node = nodeService.getLeastLoadedNode();
        if (node == null) {
            throw new IllegalStateException("No available node");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("image", image);

        pubsubClient.send(
                "node-" + node.getId().toString(),
                "CREATE_SERVER",
                payload
        );
    }
}
