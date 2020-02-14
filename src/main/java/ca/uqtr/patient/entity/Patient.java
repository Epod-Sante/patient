package ca.uqtr.patient.entity;

import ca.uqtr.patient.entity.vo.Contact;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Date;
import java.util.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "patient", schema = "public")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
public class Patient extends BaseEntity{

    @NaturalId
    @Column(name = "file_number", nullable = false, updatable = false, unique = true)
    private String fileNumber;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "birthday")
    private Date birthday;
    @Column(name = "mother_name")
    private String motherName;
    @OneToOne(mappedBy="patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Contact contact;
    @Type(type = "jsonb")
    @Column(name = "family_doctor", columnDefinition = "jsonb")
    private String familyDoctor;
    @Type(type = "jsonb")
    @Column(name = "pharmacy", columnDefinition = "jsonb")
    private String pharmacy;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "patient_professional",
            joinColumns = {@JoinColumn(name = "patient_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "professional_id", referencedColumnName = "id")})
    private Set<Professional> professionals = new HashSet<>();
    @Column(name = "is_active")
    private Boolean isActive;

    public Patient(String firstName, String lastName, Date birthday, Boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.isActive = isActive;
    }

    public Patient(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setFileNumber(String firstName, String lastName, Date birthday) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(birthday);
        int day = cal.get(Calendar.DAY_OF_MONTH) ;
        int month = (cal.get(Calendar.MONTH) + 1);
        int year = cal.get(Calendar.YEAR);
        this.fileNumber = firstName.toUpperCase().substring(0, 3) +
                lastName.toUpperCase().substring(0, 3) +
                (day<10?("0"+day):(day))+ (month<10?("0"+month):(month)) + year +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}