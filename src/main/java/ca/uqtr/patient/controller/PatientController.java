package ca.uqtr.patient.controller;

import ca.uqtr.patient.dto.MedicalFileDto;
import ca.uqtr.patient.dto.PatientDto;
import ca.uqtr.patient.dto.UserRequestDto;
import ca.uqtr.patient.dto.medicalfile.clinical_examination.ClinicalExaminationDto;
import ca.uqtr.patient.entity.Patient;
import ca.uqtr.patient.service.patient.PatientService;
import ca.uqtr.patient.service.professional.ProfessionalService;
import ca.uqtr.patient.utils.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class PatientController {

    private PatientService patientService;
    private ProfessionalService professionalService;

    @Autowired
    public PatientController(PatientService patientService, ProfessionalService professionalService) {
        this.patientService = patientService;
        this.professionalService = professionalService;
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public ResponseEntity<PatientDto> addPatient(@RequestBody PatientDto patient, HttpServletRequest request)  {
        String token = request.getHeader("Authorization").replace("bearer ","");
        System.out.println(patient.toString());
        return new ResponseEntity<>(patientService.addPatient(patient, JwtTokenUtil.getUsername(token)), HttpStatus.CREATED);
    }

/*
    @PostMapping(value = "/create/professional")
    @ResponseBody
    public void addProfessional(@RequestBody UserRequestDto userRequestDto, HttpServletRequest request)  {
        String token = request.getHeader("Authorization").replace("bearer ","");
        JwtTokenUtil.getUsername(token);
        professionalService.createProfessional(userRequestDto);
    }
*/

    @PostMapping(value = "/id")
    @ResponseBody
    public PatientDto getPatient(@RequestBody PatientDto patient){
        return patientService.getPatientById(patient);
    }

    @GetMapping(value = "/all")
    @ResponseBody
    public Iterable<Patient> getPatients(){
        return patientService.getPatients();
    }

    @GetMapping(value = "/patients/professional")
    @ResponseBody
    public Iterable<Patient> getPatientsByProfessional(HttpServletRequest request)  {
        String token = request.getHeader("Authorization").replace("bearer ","");
        return patientService.getPatientsByProfessional(JwtTokenUtil.getUsername(token));
    }

    @PostMapping(value = "/patient/name")
    @ResponseBody
    public Patient getPatientByFirstNameAndLastName(@RequestBody PatientDto patient) {
        return patientService.getPatientByFirstNameAndLastName(patient);
    }

    @PostMapping(value = "/patient/socio")
    @ResponseBody
    public MedicalFileDto addSocioDemographicVariables(@RequestParam String patientId, @RequestBody String socioDemographicVariables) throws JsonProcessingException {
        return patientService.addSocioDemographicVariables(patientId, socioDemographicVariables);
    }

    @PostMapping(value = "/patient/antecedents")
    @ResponseBody
    public MedicalFileDto addAntecedents(@RequestParam String patientId, @RequestBody String antecedents) throws JsonProcessingException {
        return patientService.addAntecedents(patientId, antecedents);
    }

    @PostMapping(value = "/patient/clinicalexamination")
    @ResponseBody
    public MedicalFileDto addClinicalExamination(@RequestParam String patientId, @RequestBody ClinicalExaminationDto clinicalExaminationDto) {
        return patientService.addClinicalExamination(patientId, clinicalExaminationDto);
    }

    @PostMapping(value = "/patient/age")
    @ResponseBody
    public List<Patient> getPatientsByAge(@RequestBody PatientDto patient) {
        return patientService.getPatientsByAge(patient);
    }

    @PutMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Patient updatePatient(@RequestBody PatientDto patient){
        return patientService.updatePatient(patient);
    }

    @DeleteMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deletePatient(@RequestBody PatientDto patient){
        patientService.deleteById(patient);
    }

}
