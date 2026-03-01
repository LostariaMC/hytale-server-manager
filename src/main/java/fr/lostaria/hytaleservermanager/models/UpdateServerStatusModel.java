package fr.lostaria.hytaleservermanager.models;

import fr.lostaria.hytaleservermanager.payload.ServerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServerStatusModel {
    private ServerStatus status;
}
