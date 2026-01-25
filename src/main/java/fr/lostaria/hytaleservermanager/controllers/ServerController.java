package fr.lostaria.hytaleservermanager.controllers;

import fr.lostaria.hytaleservermanager.services.ServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    public ResponseEntity createServer() {
        serverService.startServer();
        return ResponseEntity.ok().build();
    }

}
