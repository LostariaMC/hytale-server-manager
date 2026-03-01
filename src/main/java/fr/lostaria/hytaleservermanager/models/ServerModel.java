package fr.lostaria.hytaleservermanager.models;

import fr.lostaria.hytaleservermanager.payload.ServerStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ServerModel {

    private String id;
    private String nodeId;
    private int port;
    private String image;
    private ServerStatus status;
    private Instant createdAt;

}
