package nl.gerimedica.assignment.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.aspect.Loggable;
import nl.gerimedica.assignment.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.entities.Appointment;
import nl.gerimedica.assignment.entities.Patient;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.mapper.AppointmentMapper;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.service.HospitalService;
import nl.gerimedica.assignment.service.PatientService;
import nl.gerimedica.assignment.utils.HospitalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// REQUIRES_NEW -> Ensures each service method runs in a new transaction with READ_COMMITTED isolation level
// READ_COMMITTED -> to prevent dirty reads and guarantee data consistency during concurrent access.

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
public class HospitalServiceImpl implements HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalServiceImpl.class);

    private final AppointmentRepository appointmentRepository;

    private final PatientService patientService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Creates multiple appointments at once.
     *
     * @param request Appointment details
     * @return Created appointments
     */
    @Override
    @Loggable
    @Transactional
    public List<AppointmentResponseDTO> createBulkAppointments(@Valid AppointmentRequestDTO request) {
        logger.debug("Creating bulk appointments for request: {}", request);

        // Validate the request
        validateRequest(request);

        // Find or create the patient
        Patient patient = patientService.findOrCreatePatient(request.getSsn(), request.getPatientName());

        // Create appointments
        List<Appointment> appointments = createAppointments(request, patient);

        try {
            // Save all appointments
            appointmentRepository.saveAll(appointments);
            logger.info("Successfully created {} appointments for patient with SSN: {}", appointments.size(), patient.getSsn());

            // Record usage
            HospitalUtils.recordUsage("Bulk create appointments");

            // Map appointments to DTOs
            return AppointmentMapper.toDtoList(appointments);
        } catch (Exception e) {
            logger.error("Failed to create appointments for patient with SSN: {}", patient.getSsn(), e);
            throw new AppointmentException("Failed to create appointments: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the appointment request.
     *
     * @param request Appointment request
     */
    private void validateRequest(AppointmentRequestDTO request) {
        if (request == null) {
            throw new AppointmentException("Request cannot be null");
        }
        if (request.getSsn() == null || request.getSsn().trim().isEmpty()) {
            throw new AppointmentException("SSN cannot be null or empty");
        }
        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
            throw new AppointmentException("Patient name cannot be null or empty");
        }
        if (request.getReasons() == null || request.getReasons().isEmpty()) {
            throw new AppointmentException("Reasons cannot be null or empty");
        }
        if (request.getDates() == null || request.getDates().isEmpty()) {
            throw new AppointmentException("Dates cannot be null or empty");
        }
    }



    /**
     * Converts request data into a list of appointments.
     * @param request Appointment details
     * @param patient Target patient
     * @return Appointments list
     */
    private List<Appointment> createAppointments(AppointmentRequestDTO request, Patient patient) {
        int loopSize = Math.min(request.getReasons().size(), request.getDates().size());
        return IntStream.range(0, loopSize)
                .mapToObj(i -> new Appointment(request.getReasons().get(i), request.getDates().get(i).format(DATE_TIME_FORMATTER), patient,true))
                .collect(Collectors.toList());
    }

    @Override
    @Loggable
    @Transactional(readOnly = true) // Read-only method, no database changes — improves performance.
    public List<AppointmentResponseDTO> getAppointmentsByReason(AppointmentTypeEnum reasonKeyword) {
        if (reasonKeyword == null ) {
            throw new IllegalArgumentException("Reason keyword cannot be null or empty");
        }

        List<Appointment> matchedAppointments = appointmentRepository.findAll().stream()
                .filter(ap -> Boolean.TRUE.equals(ap.getActiveFlag())) // only active appointments
                .filter(ap -> ap.getReason() != null && ap.getReason().equals(reasonKeyword))
                .collect(Collectors.toList());

        HospitalUtils.recordUsage("Get appointments by reason");
        return AppointmentMapper.toDtoList(matchedAppointments);

    }


    @Override
    @Transactional(readOnly = true) // Read-only method, no database changes — improves performance.
    public AppointmentResponseDTO findLatestAppointmentBySSN(String ssn) {
        Appointment appointment = patientService.getPatientBySsn(ssn)
                .map(Patient::getAppointments)
                .filter(appointments -> !appointments.isEmpty())
                .flatMap(appointments -> appointments.stream()
                        .filter(ap -> Boolean.TRUE.equals(ap.getActiveFlag())) // only active appointments
                        .max(Comparator.comparing(Appointment::getDate)))
                .orElse(null);

        return AppointmentMapper.toDto(appointment);
    }

    // Changed from hard delete to soft delete by marking appointments as inactive instead of removing them from the database.
    @Override
    @Loggable
    public void deleteAppointmentsBySSN(String ssn) {
        // Find the patient by SSN
        Optional<Patient> patient = patientService.getPatientBySsn(ssn);
        if (patient.isEmpty()) {
            logger.warn("No patient found with SSN: {}", ssn);
            return;
        }

        // Check if the patient has any appointments
        List<Appointment> appointments = patient.get().getAppointments(); // Use a getter instead of direct field access
        if (appointments == null || appointments.isEmpty()) {
            logger.info("No appointments found for patient with SSN: {}", ssn);
            return;
        }

        // Soft delete: mark appointments as inactive instead of removing from DB
        try {
            for (Appointment appointment : appointments) {
                appointment.setActiveFlag(false); // mark as soft-deleted
            }
            appointmentRepository.saveAll(appointments);
            //appointmentRepository.deleteAll(appointments); -> hard delete
            logger.info("Successfully soft-deleted {} appointments for patient with SSN: {}", appointments.size(), ssn);
        } catch (Exception e) {
            logger.error("Failed to soft-delete appointments for patient with SSN: {}", ssn, e);
            throw new RuntimeException("Failed to delete appointments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Read-only method, no database changes — improves performance.
    public Page<AppointmentResponseDTO> getAppointmentsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(AppointmentMapper::toDto);
    }

}