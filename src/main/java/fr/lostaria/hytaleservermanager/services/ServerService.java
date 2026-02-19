package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.entities.Server;
import fr.lostaria.hytaleservermanager.payload.hytale.GameSessionResponse;
import fr.lostaria.hytaleservermanager.pubsub.PubsubClient;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import fr.lostaria.hytaleservermanager.repositories.ServerRepository;
import fr.lostaria.hytaleservermanager.services.hytale.HytaleSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServerService {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private ServerRepository serverRepository;

    private final HytaleSessionService hytaleSessionService;
    private final NodeService nodeService;

    private final PubsubClient pubsubClient;
    private final ObjectMapper objectMapper;

    private int nextServerId = 1;
    private List<String> serverIds = new ArrayList<>();

    @Value("${hytale.profile-uuid}")
    private String hytaleProfileUuid;

    public ServerService(
            HytaleSessionService hytaleSessionService,
            NodeService nodeService,
            PubsubClient pubsubClient,
            ObjectMapper objectMapper
    ) {
        this.hytaleSessionService = hytaleSessionService;
        this.nodeService = nodeService;
        this.pubsubClient = pubsubClient;
        this.objectMapper = objectMapper;

        serverIds.add("montparnasse");   // Paris Montparnasse
        serverIds.add("saintlazare");    // Paris Saint-Lazare
        serverIds.add("austerlitz");     // Paris Austerlitz
        serverIds.add("partdieu");       // Lyon Part-Dieu
        serverIds.add("perrache");       // Lyon Perrache
        serverIds.add("flandres");       // Lille Flandres
        serverIds.add("matabiau");       // Toulouse Matabiau
        serverIds.add("chateaucreux");   // Saint-Etienne Chateaucreux
        serverIds.add("saintcharles");   // Marseille Saint-Charles
        serverIds.add("saintjean");      // Bordeaux Saint-Jean
        serverIds.add("saintlaud");      // Angers Saint-Laud
    }

    @Transactional
    public void createServer(String image) {
        Node node = nodeService.getLeastLoadedNode();
        if (node == null) {
            throw new IllegalStateException("No available node");
        }

        int port = pickPort(node);

        String serverId = (serverIds.get((int) (Math.random() * serverIds.size())) + "-" + nextServerId);
        nextServerId++;

        GameSessionResponse gs = hytaleSessionService.createGameSession(hytaleProfileUuid);

        Server server = Server.builder()
                .id(serverId)
                .node(node)
                .port(port)
                .image(image)
                .build();

        serverRepository.save(server);

        nodeRepository.save(node);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("image", image);
        payload.put("port", port);
        payload.put("serverId", serverId);

        ObjectNode authNode = objectMapper.createObjectNode();
        authNode.put("sessionToken", gs.sessionToken());
        authNode.put("identityToken", gs.identityToken());
        if (gs.expiresAt() != null) {
            authNode.put("expiresAt", gs.expiresAt());
        }

        payload.set("hytaleAuth", authNode);

        pubsubClient.send(
                "node-" + node.getId().toString(),
                "CREATE_SERVER",
                payload
        );
    }


    private int pickPort(Node node) {
        int start = node.getPortRangeStart();
        int end = node.getPortRangeEnd();

        int port = node.getNextPort();
        if (port < start || port > end) port = start;

        Set<Integer> used = new HashSet<>(serverRepository.findAllPortsByNodeId(node.getId()));

        int firstTried = port;
        while (used.contains(port)) {
            port++;
            if (port > end) port = start;

            if (port == firstTried) {
                throw new IllegalStateException("No available ports on this node");
            }
        }

        int next = port + 1;
        if (next > end) next = start;
        node.setNextPort(next);

        return port;
    }
}
