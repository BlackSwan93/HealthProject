package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.entities.Patient;

import java.util.List;
import java.util.Optional;


public interface PatientService {
    Patient savePatient(Patient patient);

    Optional<Patient> getPatientById(Long id);

    Optional<Patient> getPatientBySsn(String ssn);

    List<Patient> getAllPatients();

    void deletePatient(Long id);

    boolean existsBySsn(String ssn);

    Patient findOrCreatePatient(String ssn, String patientName);
}
