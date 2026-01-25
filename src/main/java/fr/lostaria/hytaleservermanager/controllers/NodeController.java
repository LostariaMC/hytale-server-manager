package fr.lostaria.hytaleservermanager.controllers;

import fr.lostaria.hytaleservermanager.entities.Node;
import fr.lostaria.hytaleservermanager.mappers.NodeMapper;
import fr.lostaria.hytaleservermanager.models.RegisterNodeModel;
import fr.lostaria.hytaleservermanager.payload.ErrorResponse;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import fr.lostaria.hytaleservermanager.services.NodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nodes")
public class NodeController {

    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;
    private final NodeService nodeService;

    public NodeController(NodeRepository nodeRepository, NodeMapper nodeMapper, NodeService nodeService) {
        this.nodeRepository = nodeRepository;
        this.nodeMapper = nodeMapper;
        this.nodeService = nodeService;
    }

    @PostMapping
    public ResponseEntity registerNode(@Valid @RequestBody RegisterNodeModel registerNodeModel) {
        String publicIp = registerNodeModel.getPublicIp();

        if (nodeRepository.existsByPublicIp(publicIp)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(HttpStatus.CONFLICT, "Node with public IP '" + publicIp + "' already exists."));
        }

        Node node = nodeService.createNode(registerNodeModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(nodeMapper.toModel(node));
    }

    @GetMapping
    public ResponseEntity getAllNodes() {
        return ResponseEntity.ok(nodeRepository.findAll().stream().map(nodeMapper::toModel).toList());
    }

}
