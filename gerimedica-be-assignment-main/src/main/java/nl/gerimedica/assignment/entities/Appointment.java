package nl.gerimedica.assignment.entities;

import jakarta.persistence.*;
import lombok.*;
import nl.gerimedica.assignment.enums.AppointmentTypeEnum;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment implements Serializable {

    private static final long serialVersionUID = 6218711049887463283L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private AppointmentTypeEnum reason;
    private String date;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    private Boolean activeFlag; // For soft delete


    public Appointment(AppointmentTypeEnum reason, String date, Patient patient, Boolean activeFlag) {
        this.reason = reason;
        this.date = date;
        this.patient = patient;
        this.activeFlag = activeFlag;
    }

    public Appointment(AppointmentTypeEnum appointmentTypeEnum, LocalDate of) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        return Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason);
    }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", reason=" + reason + ", date=" + date + "}";
    }
}
