package nl.gerimedica.assignment.service;

import jakarta.validation.Valid;
import nl.gerimedica.assignment.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HospitalService {

    List<AppointmentResponseDTO> createBulkAppointments(@Valid AppointmentRequestDTO request);
    List<AppointmentResponseDTO> getAppointmentsByReason(AppointmentTypeEnum reasonKeyword);
    AppointmentResponseDTO findLatestAppointmentBySSN(String ssn);
    void deleteAppointmentsBySSN(String ssn);
    Page<AppointmentResponseDTO> getAppointmentsPaged(int page, int size);
}
