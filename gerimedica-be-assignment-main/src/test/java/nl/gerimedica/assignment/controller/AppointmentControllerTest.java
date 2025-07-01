package nl.gerimedica.assignment.controller;

import nl.gerimedica.assignment.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.entities.Patient;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentControllerTest {

    @Mock
    private HospitalService hospitalService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentRequestDTO requestDTO;
    private AppointmentResponseDTO responseDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new AppointmentRequestDTO("John Doe", "123-45-6789",
                Arrays.asList(AppointmentTypeEnum.CHECKUP, AppointmentTypeEnum.XRAY),
                Arrays.asList(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 11, 1)));

        responseDTO = new AppointmentResponseDTO();
        responseDTO.setReason(AppointmentTypeEnum.CHECKUP);
        responseDTO.setDate(LocalDate.of(2023, 10, 1));
    }

    @Test
    void shouldCreatePatientWithValidData() {
        Patient patient = new Patient();
        patient.setName("Jane Doe");
        patient.setSsn("111-22-3333");
        patient.setAppointments(Collections.emptyList());

        assertEquals("Jane Doe", patient.getName());
        assertEquals("111-22-3333", patient.getSsn());
        assertNotNull(patient.getAppointments());
        assertTrue(patient.getAppointments().isEmpty());
    }

    @Test
    public void shouldCreateBulkAppointmentsSuccessfully() {
        when(hospitalService.createBulkAppointments(any())).thenReturn(Collections.singletonList(responseDTO));

        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.createBulkAppointments(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CHECKUP", response.getBody().get(0).getReason().getValue());
        verify(hospitalService).createBulkAppointments(any());
    }

    @Test
    public void shouldGetAppointmentsByReason() {
        when(hospitalService.getAppointmentsByReason(AppointmentTypeEnum.CHECKUP)).thenReturn(Collections.singletonList(responseDTO));

        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAppointmentsByReason("CHECKUP");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CHECKUP", response.getBody().get(0).getReason().getValue());
        verify(hospitalService).getAppointmentsByReason(AppointmentTypeEnum.CHECKUP);
    }

    @Test
    public void shouldReturnEmptyListWhenNoAppointmentsFound() {
        when(hospitalService.getAppointmentsByReason(AppointmentTypeEnum.CHECKUP)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAppointmentsByReason("CHECKUP");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(hospitalService).getAppointmentsByReason(AppointmentTypeEnum.CHECKUP);
    }

    @Test
    public void shouldDeleteAppointmentsBySSNSuccessfully() {
        doNothing().when(hospitalService).deleteAppointmentsBySSN("123-45-6789");

        ResponseEntity<String> response = appointmentController.deleteAppointmentsBySSN("123-45-6789");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All appointments associated with SSN 123-45-6789 have been deleted successfully", response.getBody());
        verify(hospitalService).deleteAppointmentsBySSN("123-45-6789");
    }

    @Test
    public void shouldHandleDeleteFailureGracefully() {
        doThrow(new AppointmentException("Failed to delete appointments"))
                .when(hospitalService).deleteAppointmentsBySSN("123-45-6789");

        assertThrows(AppointmentException.class, () -> appointmentController.deleteAppointmentsBySSN("123-45-6789"));
        verify(hospitalService).deleteAppointmentsBySSN("123-45-6789");
    }

    @Test
    public void shouldReturnLatestAppointment() {
        when(hospitalService.findLatestAppointmentBySSN("123-45-6789")).thenReturn(responseDTO);

        ResponseEntity<AppointmentResponseDTO> response = appointmentController.getLatestAppointment("123-45-6789");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CHECKUP", response.getBody().getReason().getValue());
        verify(hospitalService).findLatestAppointmentBySSN("123-45-6789");
    }

    @Test
    public void shouldReturnNullWhenNoLatestAppointmentFound() {
        when(hospitalService.findLatestAppointmentBySSN("123-45-6789")).thenReturn(null);

        ResponseEntity<AppointmentResponseDTO> response = appointmentController.getLatestAppointment("123-45-6789");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(hospitalService).findLatestAppointmentBySSN("123-45-6789");
    }
}
