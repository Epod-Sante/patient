package ca.uqtr.patient.dto;

import ca.uqtr.patient.dto.ErrorDto;
import ca.uqtr.patient.dto.patient.ContactDto;
import ca.uqtr.patient.dto.patient.FamilyDoctorDto;
import ca.uqtr.patient.dto.patient.PharmacyDto;
import ca.uqtr.patient.dto.patient.ProfessionalDto;
import ca.uqtr.patient.entity.Patient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.lang.Nullable;

import java.sql.Date;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {

    @Nullable
    private String id;
    @Nullable
    private String fileNumber;
    @Nullable
    private String firstName;
    @Nullable
    private String lastName;
    @Nullable
    private Date birthday;
    @Nullable
    private String motherName;
    @Nullable
    private ContactDto contact;
    @Nullable
    private List<FamilyDoctorDto> familyDoctor = new ArrayList<>();
    @Nullable
    private List<PharmacyDto> pharmacy = new ArrayList<>();
    @Nullable
    private List<ProfessionalDto> professionals = new ArrayList<>();
    @Nullable
    private Boolean isActive;
    @Nullable
    MedicalFileDto medicalFile;
    @Nullable
    private ErrorDto error;

    public Patient dtoToObj(ModelMapper modelMapper) {
        return modelMapper.map(this, Patient.class);
    }

    public UUID getId() {
        if (id != null)
            return UUID.fromString(id);
        else
            return null;
    }
}