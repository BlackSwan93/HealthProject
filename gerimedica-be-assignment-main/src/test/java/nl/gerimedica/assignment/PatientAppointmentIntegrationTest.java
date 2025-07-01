package nl.gerimedica.assignment;

import nl.gerimedica.assignment.entities.Appointment;
import nl.gerimedica.assignment.entities.Patient;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PatientAppointmentIntegrationTest {

        // Real repository beans are used here since this is an integration test, not a unit test.
        @Autowired
        private PatientRepository patientRepository;

        @Autowired
        private AppointmentRepository appointmentRepository;

    public void init_testSavePatientWithAppointments(){
        Patient patient = new Patient("John Doe", "123-45-6789");

        Appointment app1 = new Appointment(AppointmentTypeEnum.CHECKUP, LocalDate.of(2025, 6, 15));
        Appointment app2 = new Appointment(AppointmentTypeEnum.FOLLOWUP, LocalDate.of(2025, 6, 20));

        app1.setPatient(patient);
        app2.setPatient(patient);

        patient.setAppointments(Arrays.asList(app1, app2));
        patientRepository.save(patient);
    }

    @Test
    public void testSavePatientWithAppointments() {
        // given
        init_testSavePatientWithAppointments();

        // then
        List<Appointment> appointments = appointmentRepository.findAll();
        assertThat(appointments).hasSize(2);
        assertThat(appointments.get(0).getPatient().getSsn()).isEqualTo("123-45-6789");
    }

    public void init_testCascadeDeletePatientAlsoDeletesAppointments(){

    }

    @Test
    public void testCascadeDeletePatientAlsoDeletesAppointments() {
        // given
        Patient patient = new Patient("Jane Smith", "999-99-9999");
        Appointment app = new Appointment(AppointmentTypeEnum.FOLLOWUP, LocalDate.of(2025, 7, 1));
        app.setPatient(patient);
        patient.setAppointments(List.of(app));
        patientRepository.save(patient);

        // pre-condition
        assertThat(appointmentRepository.count()).isEqualTo(1);

        // when
        patientRepository.delete(patient);

        // then
        assertThat(appointmentRepository.count()).isZero();
    }
}
