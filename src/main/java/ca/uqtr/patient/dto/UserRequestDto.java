package ca.uqtr.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto implements Serializable {

    static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;


}
