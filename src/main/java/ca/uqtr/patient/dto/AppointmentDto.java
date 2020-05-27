package ca.uqtr.patient.dto;

import ca.uqtr.patient.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDto {
    private String id;
    private String patientId;
    private String professionalId;
    private Timestamp creationDate;
    private boolean jpaqEmail;
    private Timestamp appointmentDate;

    public Appointment dtoToObj(ModelMapper modelMapper) {
        return modelMapper.map(this, Appointment.class);
    }

    public UUID getId() {
        if (id != null)
            return UUID.fromString(id);
        else
            return null;
    }

    public UUID getPatientId() {
        if (patientId != null)
            return UUID.fromString(patientId);
        else
            return null;
    }

    public UUID getProfessionalId() {
        if (professionalId != null)
            return UUID.fromString(professionalId);
        else
            return null;
    }

}
