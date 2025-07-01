package nl.gerimedica.assignment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class AppointmentRequestDTO {
    @NotNull
    private String patientName;
    @NotNull
    private String ssn;
    @NotNull
    private List<AppointmentTypeEnum> reasons;
    @Valid
    private List<@FutureOrPresent LocalDate> dates;

    public AppointmentRequestDTO(String johnDoe, String s, List<AppointmentTypeEnum> list, List<LocalDate> list1) {
    }
}
