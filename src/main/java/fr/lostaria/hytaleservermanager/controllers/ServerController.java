package fr.lostaria.hytaleservermanager.controllers;

import fr.lostaria.hytaleservermanager.entities.Server;
import fr.lostaria.hytaleservermanager.mappers.ServerMapper;
import fr.lostaria.hytaleservermanager.models.CreateServerModel;
import fr.lostaria.hytaleservermanager.payload.ErrorResponse;
import fr.lostaria.hytaleservermanager.repositories.ServerRepository;
import fr.lostaria.hytaleservermanager.services.ServerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
public class ServerController {

    private final ServerRepository serverRepository;
    private final ServerMapper serverMapper;
    private final ServerService serverService;

    public ServerController(
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
        Server server = serverService.createServer(
                createServerModel.getImage()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serverMapper.toModel(server));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteServer(@PathVariable String id) {
        return serverRepository.findById(id)
                .map(server -> {
                    serverService.deleteServer(server);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(
                                        HttpStatus.NOT_FOUND,
                                        "Server not found"
                                ))
                );
    }

}
