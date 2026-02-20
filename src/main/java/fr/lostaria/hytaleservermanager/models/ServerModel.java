package fr.lostaria.hytaleservermanager.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerModel {

    private String id;
    private String nodeId;
    private int port;
    private String image;

}
