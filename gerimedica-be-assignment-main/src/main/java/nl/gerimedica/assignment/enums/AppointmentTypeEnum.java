package nl.gerimedica.assignment.enums;

import lombok.Getter;

@Getter
public enum AppointmentTypeEnum {

  CHECKUP(1, "CHECKUP"), FOLLOWUP(2, "FOLLOWUP"), XRAY(3, "XRAY");

    private Integer key;
    private String value;


    AppointmentTypeEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }


}
