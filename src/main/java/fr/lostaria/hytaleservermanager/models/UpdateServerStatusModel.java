package fr.lostaria.hytaleservermanager.models;

import fr.lostaria.hytaleservermanager.payload.ServerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServerStatusModel {
    @NotNull
    private ServerStatus status;
}
