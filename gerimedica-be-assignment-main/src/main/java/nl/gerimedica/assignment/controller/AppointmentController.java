package nl.gerimedica.assignment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.constants.Paths;
import nl.gerimedica.assignment.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.dto.ErrorResponseDTO;
import nl.gerimedica.assignment.entities.Appointment;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.service.HospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * REST Controller for managing hospital appointments.
 * Provides endpoints for creating, retrieving, and managing appointments.
 */
@RestController
@RequestMapping(Paths.ROUTE_PATH)
@Tag(name = "Appointment Management", description = "Endpoints for managing hospital appointments")
@Validated
@RequiredArgsConstructor
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final HospitalService hospitalService;

    private final AppointmentRepository appointmentRepository;

    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");



    //curl -X POST http://localhost:8585/api/bulk-appointments -H "Content-Type: application/json" -d "{\"patientName\":\"John Doe\",\"ssn\":\"123-45-6789\",\"reasons\":[\"CHECKUP\"],\"dates\":[\"2025-07-01\"]}"
    @Operation(summary = "Create bulk appointments")
    @ApiResponse(responseCode = "201", description = "Appointments created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(
            value = Paths.BULK_APPOINTMENTS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AppointmentResponseDTO>> createBulkAppointments(
            @Valid @RequestBody AppointmentRequestDTO request) {
        logger.debug("REST request to create bulk appointments : {}", request);

        // Basic input validation for request before processing
        if (request == null
                || request.getSsn() == null || request.getSsn().trim().isEmpty()
                || request.getPatientName() == null || request.getPatientName().trim().isEmpty()
                || request.getReasons() == null || request.getReasons().isEmpty()
                || request.getDates() == null || request.getDates().isEmpty()) {
            logger.warn("Invalid request body for bulk appointment creation");
            throw new AppointmentException("Invalid request! Please provide valid SSN, patient name, reasons and dates.");
        }

        try {
            List<AppointmentResponseDTO> createdAppointments = hospitalService.createBulkAppointments(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdAppointments);
        } catch (AppointmentException e) {
            logger.error("Error creating bulk appointments", e);
            throw e;
        }
    }

    @Operation(summary = "Lists all appointments")
    @GetMapping(value = Paths.GET_ALL_APPOINTMENTS)
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        if (appointments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(appointments);
    }

    /**
     * Returns appointments matching the given reason.
     *
     * @param keyword reason to search for
     * @return matching appointments
     * "http://localhost:8585/api/appointments-by-reason?keyword=CHEcKUp"
     */
    @Operation(summary = "Get appointments by reason")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments retrieved by reason"),
            @ApiResponse(responseCode = "400", description = "Invalid keyword"),
            @ApiResponse(responseCode = "404", description = "No appointments found by reason")
    })
    @GetMapping(value = Paths.APPOINTMENT_BY_REASON, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByReason( @RequestParam String keyword) {
        logger.debug("Fetching appointments for reason containing: {}", keyword);
        AppointmentTypeEnum reason;
        //Validates the keyword safely by catching invalid enum values and throwing a clear, controlled exception instead of failing at runtime.
        try {
            reason = AppointmentTypeEnum.valueOf(keyword.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid appointment reason keyword: {}", keyword);
            throw new AppointmentException("Invalid appointment reason: " + keyword);
        }
        List<AppointmentResponseDTO> appointments = hospitalService.getAppointmentsByReason(reason);
        return ResponseEntity.ok(appointments);
    }


    @Operation(summary = "Delete appointments by SSN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments soft-deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid SSN format"),
            @ApiResponse(responseCode = "404", description = "No appointments exist for this SSN")
    })
    @DeleteMapping(Paths.DELETE_APPOINTMENTS)
    public ResponseEntity<String> deleteAppointmentsBySSN(@RequestParam(required = true) String ssn) {
        logger.debug("Marking all appointments as inactive for SSN: {}", ssn);
        validateSSNFormat(ssn);
        try {
            hospitalService.deleteAppointmentsBySSN(ssn);
            String message = String.format("All appointments associated with SSN %s have been deleted successfully", ssn);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Failed to delete appointments for SSN: {}", ssn, e);
            throw new AppointmentException("Failed to delete appointments: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the latest appointment for a given SSN.
     *
     * @param ssn The Social Security Number
     * @return The latest appointment
     */
    @Operation(summary = "Get latest appointment by SSN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest appointment found"),
            @ApiResponse(responseCode = "400", description = "Invalid SSN"),
            @ApiResponse(responseCode = "404", description = "No appointment found")
    })
    @GetMapping(value= Paths.LATEST_APPOINTMENTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppointmentResponseDTO> getLatestAppointment(@RequestParam String ssn) {
        logger.debug("Fetching latest appointment for SSN: {}", ssn);
        validateSSNFormat(ssn);
        try {
            AppointmentResponseDTO latest = hospitalService.findLatestAppointmentBySSN(ssn);
            return ResponseEntity.ok(latest);
        } catch (Exception e) {
            logger.error("Failed to fetch latest appointment for SSN: {}", ssn, e);
            throw new AppointmentException("Failed to fetch latest appointment: " + e.getMessage(), e);
        }
    }

    // I added pagination support to avoid returning too much data at once and make the API more scalable.
    @GetMapping(value= Paths.PAGED_APPOINTMENTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointmentsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AppointmentResponseDTO> result = hospitalService.getAppointmentsPaged(page, size);

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    // SSN format check to ensure the input follows the U.S. standard (XXX-XX-XXXX)
    private void validateSSNFormat(String ssn) {
        if (ssn == null || !SSN_PATTERN.matcher(ssn).matches()) {
            throw new AppointmentException("Invalid SSN format. Expected format is XXX-XX-XXXX.");
        }
    }
}
