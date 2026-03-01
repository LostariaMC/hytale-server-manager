package fr.lostaria.hytaleservermanager.controllers;

import fr.lostaria.hytaleservermanager.entities.Server;
import fr.lostaria.hytaleservermanager.mappers.ServerMapper;
import fr.lostaria.hytaleservermanager.models.CreateServerModel;
import fr.lostaria.hytaleservermanager.models.UpdateServerStatusModel;
import fr.lostaria.hytaleservermanager.payload.ErrorResponse;
import fr.lostaria.hytaleservermanager.repositories.ServerRepository;
import fr.lostaria.hytaleservermanager.services.ServerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
public class ServersController {

    private final ServerRepository serverRepository;
    private final ServerMapper serverMapper;
    private final ServerService serverService;

    public ServersController(
            ServerRepository serverRepository,
            ServerMapper serverMapper,
            ServerService serverService
    ) {
        this.serverRepository = serverRepository;
        this.serverMapper = serverMapper;
        this.serverService = serverService;
    }

    @PostMapping
    public ResponseEntity createServer(
            @Valid @RequestBody CreateServerModel createServerModel
    ) {
        Server server = serverService.createServer(createServerModel.getImage());
        return ResponseEntity.status(HttpStatus.CREATED).body(serverMapper.toModel(server));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteServer(@PathVariable String id) {
        return serverRepository.findById(id)
                .map(server -> {
                    serverService.deleteServer(server);
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(
                                        HttpStatus.NOT_FOUND,
                                        "Server not found"
                                ))
                );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity updateServerStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateServerStatusModel body
    ) {
        return serverRepository.findById(id)
                .map(server -> {
                    Server updated = serverService.updateStatus(server, body.getStatus());
                    return (ResponseEntity) ResponseEntity.ok()
                            .body(serverMapper.toModel(updated));
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(HttpStatus.NOT_FOUND, "Server not found"))
                );
    }

}
