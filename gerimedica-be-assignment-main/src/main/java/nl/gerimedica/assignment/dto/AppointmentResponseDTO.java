package nl.gerimedica.assignment.dto;

import lombok.Data;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    private Long id;
    private AppointmentTypeEnum reason;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
