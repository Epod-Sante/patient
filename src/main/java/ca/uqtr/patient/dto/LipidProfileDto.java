package ca.uqtr.patient.dto;

import ca.uqtr.patient.entity.LipidProfile;
import ca.uqtr.patient.entity.MedicalFile;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import javax.persistence.Column;
import java.sql.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class LipidProfileDto {
    private String id;
    private double ldl;
    private double hdl;
    private double nohdl;
    private double triglyceride;
    private double hba1c;
    private Date date ;
    private double glucoseJeun;
    private double glucoseAleatoire;

    public LipidProfile dtoToObj(ModelMapper modelMapper) {
        return modelMapper.map(this, LipidProfile.class);
    }

    public UUID getId() {
        if (id != null)
            return UUID.fromString(id);
        else
            return null;
    }
}
