package ca.uqtr.patient.dto.medicalfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocioDemographicVariablesDto {

    private int age;
    private String civilStatus;
    private String gender;
    private String familyIncome;
    private String jobStatus;
    private String education;
    private String livingEnvironment;
    private String housingType;

}
