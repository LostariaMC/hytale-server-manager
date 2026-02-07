package fr.lostaria.hytaleservermanager.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterNodeModel {

    private String ip;
    private int portRangeStart;
    private int portRangeEnd;

}
