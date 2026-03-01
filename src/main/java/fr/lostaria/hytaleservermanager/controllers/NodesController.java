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
public class NodesController {

    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;
    private final NodeService nodeService;

    public NodesController(NodeRepository nodeRepository, NodeMapper nodeMapper, NodeService nodeService) {
        this.nodeRepository = nodeRepository;
        this.nodeMapper = nodeMapper;
        this.nodeService = nodeService;
    }

    @PostMapping
    public ResponseEntity registerNode(@Valid @RequestBody RegisterNodeModel registerNodeModel) {
        if(registerNodeModel.getPortRangeStart() > registerNodeModel.getPortRangeEnd()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Port range start must be less than port range end"
                    ));
        }

        String ip = registerNodeModel.getIp();
        Node node = nodeRepository.findByIp(ip)
                .orElseGet(() -> nodeService.createNode(registerNodeModel));

        return ResponseEntity.status(HttpStatus.CREATED).body(nodeMapper.toModel(node));
    }

    @GetMapping
    public ResponseEntity getAllNodes() {
        return ResponseEntity.ok(nodeRepository.findAll().stream().map(nodeMapper::toModel).toList());
    }

}
