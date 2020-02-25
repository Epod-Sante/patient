package ca.uqtr.patient.service.patient;


import ca.uqtr.patient.dto.*;
import ca.uqtr.patient.dto.Error;
import ca.uqtr.patient.dto.medicalfile.SocioDemographicVariablesDto;
import ca.uqtr.patient.dto.medicalfile.clinical_examination.ClinicalExaminationDto;
import ca.uqtr.patient.entity.*;
import ca.uqtr.patient.repository.professional.ProfessionalRepository;
import ca.uqtr.patient.repository.medicalFile.MedicalFileRepository;
import ca.uqtr.patient.repository.patient.PatientRepository;
import ca.uqtr.patient.service.questionnaire.QuestionnaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.bytecode.stackmap.TypeData;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PatientServiceImpl implements PatientService {
    private static final Logger LOGGER = Logger.getLogger( TypeData.ClassName.class.getName() );

    private PatientRepository patientRepository;
    private final MedicalFileRepository medicalFileRepository;
    private final ProfessionalRepository professionalRepository;
    private ModelMapper modelMapper;
    private MessageSource messageSource;
    private QuestionnaireService questionnaireService;

    @Autowired
    public PatientServiceImpl(PatientRepository patientRepository, ModelMapper modelMapper, MedicalFileRepository medicalFileRepository, ProfessionalRepository professionalRepository, MessageSource messageSource, QuestionnaireService questionnaireService) {
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
        this.medicalFileRepository = medicalFileRepository;
        this.professionalRepository = professionalRepository;
        this.messageSource = messageSource;
        this.questionnaireService = questionnaireService;
    }

    @Override
    public Response addPatient(PatientDto patientDto, String professionalId) {
        try {
            System.out.println(patientDto);
            System.out.println(professionalId);
                Patient patient = patientDto.dtoToObj(modelMapper);
                Professional professional = professionalRepository.getProfessionalById(UUID.fromString(professionalId));
                if (professional == null){
                    professional = professionalRepository.save(new Professional(UUID.fromString(professionalId), true));
                }
                Set<Professional> professionals = patient.getProfessionals();
                professionals.add(professional);
                patient.setProfessionals(professionals);
                patient.setFileNumber();
                Patient patient_db = patientRepository.save(patient);

                MedicalFile medicalFile = new MedicalFile();
                medicalFile.setPatient(patient_db.getId().toString());
                medicalFile.setCreationDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
                medicalFileRepository.save(medicalFile);
                PatientDto patientDto1 = modelMapper.map(patient_db, PatientDto.class);
                questionnaireService.sendQuestionnaire(patientDto1);

                return new Response(patientDto1, null);

        } catch (Exception ex){
            LOGGER.log( Level.WARNING, ex.getMessage());
            System.out.println(ex);
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public Response getPatient(String patientId) {
        try {
            PatientDto patientDto = new PatientDto();
            patientDto.setId(patientId);
            Optional<Patient> patient = patientRepository.findById(UUID.fromString(patientId));
            MedicalFile medicalFile = medicalFileRepository.getMedicalFileByPatient(patientId);
            System.out.println(medicalFile.toString());
            Type medicalFileHistoryType = new TypeToken<List<MedicalFileHistoryDto>>() {}.getType();
            List<MedicalFileHistoryDto> medicalFileHistoryDtoList = modelMapper.map(medicalFile.getMedicalFileHistory(), medicalFileHistoryType);
            Type lipidProfileType = new TypeToken<List<LipidProfileDto>>() {}.getType();
            List<LipidProfileDto> lipidProfileDtoList = modelMapper.map(medicalFile.getLipidProfiles(), lipidProfileType);

            MedicalFileDto medicalFileDto = modelMapper.map(medicalFile, MedicalFileDto.class);
            medicalFileDto.setMedicalFileHistory(medicalFileHistoryDtoList);
            medicalFileDto.setLipidProfiles(lipidProfileDtoList);
            patientDto.setMedicalFile(medicalFileDto);
            System.out.println(patientDto.toString());
            return patient.map(value -> new Response(modelMapper.map(value, PatientDto.class), null)).orElseGet(() -> new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US))));

        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public Response getPatients() {
        return new Response(patientRepository.findAll(), null);
    }

    @Override
    public Response getPatientsByProfessional(String id) {
        return new Response(patientRepository.findByProfessionals(professionalRepository.getProfessionalById(UUID.fromString(id))), null);
    }

    @Override
    public Response getPatientSocioDemographicVariables(String patientId) throws IOException {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileByPatient(patientId);
        String socio = medicalFile.getSocioDemographicVariables();
        if (socio == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.socio.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.socio.exist.message", null, Locale.US)));
        //return new Response(modelMapper.map(socio, SocioDemographicVariablesDto.class), null);
        ObjectMapper mapper = new ObjectMapper();
        SocioDemographicVariablesDto socioDemographicVariablesDto = mapper.readValue(socio, SocioDemographicVariablesDto.class);
        return new Response(socioDemographicVariablesDto, null);
    }

    @Override
    public Response addSocioDemographicVariables(String patientId, String socioDemographicVariablesDto) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileByPatient(patientId);
        medicalFile.setSocioDemographicVariables(socioDemographicVariablesDto);
        return new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
    }

    @Override
    public Response getPatientAntecedents(String patientId) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_MedicalFileHistory_FetchTypeEAGER(patientId);
        List<MedicalFileHistory> medicalFileHistories = medicalFile.getMedicalFileHistory();
        if (medicalFileHistories == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.antecedents.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.antecedents.exist.message", null, Locale.US)));
        return new Response(medicalFileHistories, null);
    }

    @Override
    public Response addAntecedents(String patientId, String antecedentsDto) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_MedicalFileHistory_FetchTypeEAGER(patientId);
        MedicalFileHistory medicalFileHistory = new MedicalFileHistory(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), antecedentsDto);
        List<MedicalFileHistory> medicalFileHistories = medicalFile.getMedicalFileHistory();
        medicalFileHistories.add(medicalFileHistory);
        medicalFile.setMedicalFileHistory(medicalFileHistories);
        return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
    }

    @Override
    public Response getPatientClinicalExaminationList(String patientId) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_ClinicalExamination_FetchTypeEAGER(patientId);
        System.out.println(medicalFile.toString());
        List<ClinicalExamination> clinicalExamination = medicalFile.getClinicalExamination();
        if (clinicalExamination == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.ce.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.ce.exist.message", null, Locale.US)));
        return new Response(clinicalExamination, null);
    }

    @Override
    public Response addClinicalExamination(String patientId, ClinicalExaminationDto clinicalExaminationDto) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_ClinicalExamination_FetchTypeEAGER(patientId);
        System.out.println(medicalFile.toString());
        List<ClinicalExamination> clinicalExamination = medicalFile.getClinicalExamination();
        clinicalExamination.add(clinicalExaminationDto.dtoToObj(modelMapper));
        medicalFile.setClinicalExamination(clinicalExamination);
        return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
    }

    @Override
    public Response updatePatient(PatientDto patientDto) {
        try {
            Patient patient = patientDto.dtoToObj(modelMapper);
            patientRepository.save(patient);
            return new Response(modelMapper.map(modelMapper.map(patient, PatientDto.class), PatientDto.class), null);
        } catch (Exception ex){
            LOGGER.log( Level.WARNING, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public Response getPatientLipidProfile(String patientId) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_LipidProfile_FetchTypeEAGER(patientId);
        System.out.println(medicalFile.toString());
        List<LipidProfile> lipidProfiles = medicalFile.getLipidProfiles();
        if (lipidProfiles == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.ce.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.ce.exist.message", null, Locale.US)));
        return new Response(lipidProfiles, null);
    }

    @Override
    public Response addLipidProfile(String patientId, LipidProfileDto lipidProfileDto) {
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_LipidProfile_FetchTypeEAGER(patientId);
        System.out.println(medicalFile.toString());
        List<LipidProfile> lipidProfiles = medicalFile.getLipidProfiles();
        lipidProfiles.add(lipidProfileDto.dtoToObj(modelMapper));
        medicalFile.setLipidProfiles(lipidProfiles);
        return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
    }

    @Override
    public void createQuestionnaireToken(String patientId, String token) {
        Patient patient = patientRepository.getPatientById(UUID.fromString(patientId));
        if (patient != null){
            patient.setQuestionnaireToken(token);
            patient.setQuestionnaireTokenExpirationDate(new java.sql.Date (Calendar.getInstance().getTime().getTime()));
            patientRepository.save(patient);
        }

    }

}
