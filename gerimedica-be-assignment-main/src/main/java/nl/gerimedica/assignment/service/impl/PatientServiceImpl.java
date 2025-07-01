package nl.gerimedica.assignment.service.impl;

import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.aspect.Loggable;
import nl.gerimedica.assignment.entities.Patient;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.repository.PatientRepository;
import nl.gerimedica.assignment.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the Hospital Service interface.
 * Handles business logic for appointment management.
 */

// REQUIRES_NEW -> Ensures each service method runs in a new transaction with READ_COMMITTED isolation level
// READ_COMMITTED -> to prevent dirty reads and guarantee data consistency during concurrent access.
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);


        private final PatientRepository patientRepository;


        @Override
        @Loggable
        public Patient savePatient(Patient patient) {
            return patientRepository.save(patient);
        }

        @Override
        public Optional<Patient> getPatientById(Long id) {
            return patientRepository.findById(id);
        }

        @Override
        public Optional<Patient> getPatientBySsn(String ssn) {
            return patientRepository.findBySsn(ssn);
        }

        @Override
        public List<Patient> getAllPatients() {
            return patientRepository.findAll();
        }


        @Override
        @Loggable
        public void deletePatient(Long id) {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new AppointmentException("Patient not found with id: " + id));

            patientRepository.delete(patient);
        }

        @Override
        public boolean existsBySsn(String ssn) {
            return patientRepository.findBySsn(ssn).isPresent();
        }

        @Override
        public Patient findOrCreatePatient(String ssn, String patientName) {
            return patientRepository.findBySsn(ssn)
                    .orElseGet(() -> {
                        logger.info("Creating new patient with SSN: {}", ssn);
                        return patientRepository.save(new Patient(patientName, ssn));
                    });
        }
    }
