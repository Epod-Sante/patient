package ca.uqtr.patient.service.patient;

import ca.uqtr.patient.dto.*;
import ca.uqtr.patient.dto.medicalfile.clinical_examination.ClinicalExaminationDto;
import ca.uqtr.patient.entity.Patient;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PatientService {

    Response addPatient(PatientDto patientDto, String professionalId);
    Response getPatient(String patientId);
    Response getPatients();
    Response getPatientsByProfessional(String id);
    //Response getPatientSocioDemographicVariables(String patientId) throws IOException;
    Response addSocioDemographicVariables(String patientId, String socioDemographicVariablesDto) throws JsonProcessingException;
    Response getPatientAntecedents(String patientId);
    Response addAntecedents(String patientId, String antecedentsDto) throws JsonProcessingException;
    Response getPatientClinicalExaminationList(String patientId);
    Response updateClinicalExamination(String patientId, ClinicalExaminationDto clinicalExaminationDto);
    Response updatePatient(PatientDto patient);

    Response getPatientLipidProfile(String patientId);
    Response addLipidProfile(String patientId, LipidProfileDto lipidProfileDto);

    void createQuestionnaireToken(String patientId, String token);

    Patient getPatientByQuestionnaireToken(String token);

    Response patientLogin(PatientDto patient);

    ProfileDto getPatientInfos(String patientId);

    Response addIndividualQuestionnaire(IndividualQuestionnaireDto individualQuestionnaireDto);
}
