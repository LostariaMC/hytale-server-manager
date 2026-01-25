package fr.lostaria.hytaleservermanager.controllers;

import fr.lostaria.hytaleservermanager.payload.ErrorResponse;
import fr.lostaria.hytaleservermanager.repositories.NodeRepository;
import fr.lostaria.hytaleservermanager.services.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final NodeRepository nodeRepository;
    private final MessageService messageService;

    public MessageController(NodeRepository nodeRepository, MessageService messageService) {
        this.nodeRepository = nodeRepository;
        this.messageService = messageService;
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity messageListener(@PathVariable String nodeId, @RequestParam(defaultValue = "25") int timeoutSeconds) {
        if (!nodeRepository.existsById(nodeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND, "Node not found."));
        }

        int t = Math.max(1, Math.min(timeoutSeconds, 30));
        String msg = messageService.waitNext(nodeId, Duration.ofSeconds(t));

        if (msg == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(msg);
    }

}
