package ca.uqtr.patient.service.patient;


import ca.uqtr.patient.dto.*;
import ca.uqtr.patient.dto.Error;
import ca.uqtr.patient.dto.medicalfile.clinical_examination.ClinicalExaminationDto;
import ca.uqtr.patient.entity.*;
import ca.uqtr.patient.entity.view.Birthday_gender;
import ca.uqtr.patient.entity.view.Height_weight;
import ca.uqtr.patient.repository.clinicalExamination.ClinicalExaminationRepository;
import ca.uqtr.patient.repository.professional.ProfessionalRepository;
import ca.uqtr.patient.repository.medicalFile.MedicalFileRepository;
import ca.uqtr.patient.repository.patient.PatientRepository;
import ca.uqtr.patient.repository.view.Birthday_genderRepository;
import ca.uqtr.patient.repository.view.Height_weightRepository;
import ca.uqtr.patient.service.questionnaire.QuestionnaireService;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class PatientServiceImpl implements PatientService {
    private static final Logger LOGGER = Logger.getLogger( PatientServiceImpl.class.getName() );

    private PatientRepository patientRepository;
    private MedicalFileRepository medicalFileRepository;
    private ProfessionalRepository professionalRepository;
    private ModelMapper modelMapper;
    private MessageSource messageSource;
    private QuestionnaireService questionnaireService;
    private Height_weightRepository height_weightRepository;
    private Birthday_genderRepository birthday_genderRepository;
    private ClinicalExaminationRepository clinicalExaminationRepository;
    @Value("${sha3-256.salt}")
    private String SALT;
    public static final String SHA3_256 = "SHA3-256";

    @Autowired
    public PatientServiceImpl(PatientRepository patientRepository, ModelMapper modelMapper, MedicalFileRepository medicalFileRepository, ProfessionalRepository professionalRepository, MessageSource messageSource, QuestionnaireService questionnaireService, Height_weightRepository height_weightRepository, Birthday_genderRepository birthday_genderRepository, ClinicalExaminationRepository clinicalExaminationRepository) {
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
        this.medicalFileRepository = medicalFileRepository;
        this.professionalRepository = professionalRepository;
        this.messageSource = messageSource;
        this.questionnaireService = questionnaireService;
        this.height_weightRepository = height_weightRepository;
        this.birthday_genderRepository = birthday_genderRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @Override
    public Response addPatient(PatientDto patientDto, String professionalId) {

            System.out.println(patientDto);
            System.out.println(professionalId);
            if (patientRepository.getPatientByContact_Email(patientDto.getContact().getEmail()) != null)
                return new Response(null,
                        new Error(Integer.parseInt(messageSource.getMessage("error.patient.email.id", null, Locale.US)),
                                messageSource.getMessage("error.patient.email.message", null, Locale.US)));
            Patient patient = patientDto.dtoToObj(modelMapper);
            Professional professional = professionalRepository.getProfessionalById(UUID.fromString(professionalId));
            if (professional == null){
                professional = professionalRepository.save(new Professional(UUID.fromString(professionalId), true));
            }
            Set<Professional> professionals = patient.getProfessionals();
            professionals.add(professional);
            patient.setProfessionals(professionals);
            patient.setFileNumber();
            patient.setLoginCode();
            Patient patient_db = patientRepository.save(patient);

            MedicalFile medicalFile = new MedicalFile();
            String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patient_db.getId().toString().concat(SALT));
            medicalFile.setPatient(patientIdSHA);
            medicalFile.setCreationDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
            medicalFileRepository.save(medicalFile);
            PatientDto patientDto1 = modelMapper.map(patient_db, PatientDto.class);
            questionnaireService.sendQuestionnaire(patientDto1);

            return new Response(patientDto1, null);


    }

    @Override
    public Response getPatient(String patientId) {
        try {
            PatientDto patientDto = modelMapper.map(patientRepository.getPatientById(UUID.fromString(patientId)), PatientDto.class);
            if (patientDto == null){
                return new Response(null,
                        new Error(Integer.parseInt(messageSource.getMessage("error.patient.login.email.id", null, Locale.US)),
                                messageSource.getMessage("error.patient.login.email.message", null, Locale.US)));
            }
            String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
            MedicalFile medicalFile = medicalFileRepository.getMedicalFileByPatient(patientIdSHA);
            Type medicalFileHistoryType = new TypeToken<List<MedicalFileHistoryDto>>() {}.getType();
            List<MedicalFileHistoryDto> medicalFileHistoryDtoList = modelMapper.map(medicalFile.getMedicalFileHistory(), medicalFileHistoryType);
            Type lipidProfileType = new TypeToken<List<LipidProfileDto>>() {}.getType();
            List<LipidProfileDto> lipidProfileDtoList = modelMapper.map(medicalFile.getLipidProfiles(), lipidProfileType);

            MedicalFileDto medicalFileDto = modelMapper.map(medicalFile, MedicalFileDto.class);
            medicalFileDto.setMedicalFileHistory(medicalFileHistoryDtoList);
            medicalFileDto.setLipidProfiles(lipidProfileDtoList);
            patientDto.setMedicalFile(medicalFileDto);
            if (patientRepository.isPatientDidAnswerBREQ(UUID.fromString(patientId)) != null)
                patientDto.setHasBREQ(true);
            return new Response(patientDto, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            LOGGER.log( Level.WARNING, e.getStackTrace().toString());
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

    /*@Override
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
    }*/

    @Override
    public Response addSocioDemographicVariables(String patientId, String socioDemographicVariablesDto) {
        Patient patient = patientRepository.getPatientById(UUID.fromString(patientId));
        patient.setSocioDemographicVariables(socioDemographicVariablesDto);
        return new Response(modelMapper.map(patientRepository.save(patient), PatientDto.class), null);
    }

    @Override
    public Response getPatientAntecedents(String patientId) {
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_MedicalFileHistory_FetchTypeEAGER(patientIdSHA);
        List<MedicalFileHistory> medicalFileHistories = medicalFile.getMedicalFileHistory();
        if (medicalFileHistories == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.antecedents.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.antecedents.exist.message", null, Locale.US)));
        return new Response(medicalFileHistories, null);
    }

    @Override
    public Response addAntecedents(String patientId, String antecedentsDto) {
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_MedicalFileHistory_FetchTypeEAGER(patientIdSHA);
        MedicalFileHistory medicalFileHistory = new MedicalFileHistory(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), antecedentsDto);
        List<MedicalFileHistory> medicalFileHistories = medicalFile.getMedicalFileHistory();
        medicalFileHistories.add(medicalFileHistory);
        medicalFile.setMedicalFileHistory(medicalFileHistories);
        return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
    }

    @Override
    public Response getPatientClinicalExaminationList(String patientId) {
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_ClinicalExamination_FetchTypeEAGER(patientIdSHA);
        System.out.println(medicalFile.toString());
        List<ClinicalExamination> clinicalExamination = medicalFile.getClinicalExamination();
        if (clinicalExamination == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.antecedents.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.antecedents.exist.message", null, Locale.US)));
        return new Response(clinicalExamination, null);
    }

    @Override
    public Response updateClinicalExamination(String patientId, ClinicalExaminationDto clinicalExaminationDto) {
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_ClinicalExamination_FetchTypeEAGER(patientIdSHA);
        List<ClinicalExamination> clinicalExamination = medicalFile.getClinicalExamination();
       /* clinicalExamination.add(clinicalExaminationDto.dtoToObj(modelMapper));
        medicalFile.setClinicalExamination(clinicalExamination);*/
/*
        for (int i = 0; i < clinicalExamination.size(); i++) {
            if (clinicalExamination.get(i).getDate().equals(clinicalExaminationDto.dtoToObj(modelMapper).getDate())){
                System.out.println("******** true");
            }else {
                System.out.println("******** false");
                System.out.println(clinicalExamination.get(i).getDate());
                System.out.println(clinicalExaminationDto.dtoToObj(modelMapper).getDate());
                System.out.println("******** ");

            }
        }
        if (clinicalExamination.stream()
                .anyMatch(obj -> obj.getDate().equals(clinicalExaminationDto.dtoToObj(modelMapper).getDate()))){
            ClinicalExamination ce = (ClinicalExamination) clinicalExamination.stream()
                    .filter(
                            obj -> obj.getDate().equals(clinicalExaminationDto.dtoToObj(modelMapper).getDate())
                    )
                    .map(obj -> {
                        clinicalExamination.remove(obj);
                        clinicalExamination.add(clinicalExaminationDto.dtoToObj(modelMapper));
                        medicalFile.setClinicalExamination(clinicalExamination);
                        return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
                    });
        }else {
            clinicalExamination.add(clinicalExaminationDto.dtoToObj(modelMapper));
            medicalFile.setClinicalExamination(clinicalExamination);
            return  new Response(modelMapper.map(medicalFileRepository.save(medicalFile), MedicalFileDto.class), null);
        }
        return new Response(null,
                new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                        messageSource.getMessage("error.null.message", null, Locale.US)));*/


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
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_LipidProfile_FetchTypeEAGER(patientIdSHA);
        List<LipidProfile> lipidProfiles = medicalFile.getLipidProfiles();
        if (lipidProfiles == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.ce.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.ce.exist.message", null, Locale.US)));
        return new Response(lipidProfiles, null);
    }

    @Override
    public Response addLipidProfile(String patientId, LipidProfileDto lipidProfileDto) {
        String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT));
        MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_LipidProfile_FetchTypeEAGER(patientIdSHA);
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

    @Override
    public Patient getPatientByQuestionnaireToken(String token) {
        return patientRepository.getPatientByQuestionnaireToken(token);
    }

    @Override
    public Response patientLogin(PatientDto patient) {
        try {
            PatientDto patientDto = modelMapper.map(patientRepository.getPatientByContact_Email(patient.getContact().getEmail()), PatientDto.class);
            if (patientDto == null){
                return new Response(null,
                        new Error(Integer.parseInt(messageSource.getMessage("error.patient.login.email.id", null, Locale.US)),
                                messageSource.getMessage("error.patient.login.email.message", null, Locale.US)));
            }
            if (!patientDto.getLoginCode().equals(patient.getLoginCode())){
                return new Response(null,
                        new Error(Integer.parseInt(messageSource.getMessage("error.login_code.id", null, Locale.US)),
                                messageSource.getMessage("error.login_code.message", null, Locale.US)));
            }
            if(patientRepository.isPatientDidAnswerBREQ(patientDto.getId()) != null)
                patientDto.setHasBREQ(true);
            return new Response(patientDto, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public ProfileDto getPatientInfos(String patientId) {
        Optional<Birthday_gender> birthday_gender = birthday_genderRepository.findById(UUID.fromString(patientId));
        Height_weight height_weight = height_weightRepository.getByPatientId(new DigestUtils(SHA3_256).digestAsHex(patientId.concat(SALT)));
        ProfileDto profileDto = new ProfileDto();
        if (height_weight == null){
            profileDto.setHeight(null);
            profileDto.setWeight(null);
            return profileDto;
        }
        profileDto.setBirthday(birthday_gender.map(Birthday_gender::getBirthday).orElse(null));
        profileDto.setGender(birthday_gender.map(Birthday_gender::getGender).orElse(null));
        profileDto.setHeight(height_weight.getHeight());
        profileDto.setWeight(height_weight.getWeight());
        System.out.println("------ profile dto= "+ profileDto.toString());
        return profileDto;
    }

    @Override
    public Response addIndividualQuestionnaire(IndividualQuestionnaireDto individualQuestionnaireDto) {
        try {
            assert individualQuestionnaireDto.getPatientId() != null;
            Patient patient = patientRepository.getPatientById(UUID.fromString(individualQuestionnaireDto.getPatientId()));
            String patientIdSHA = new DigestUtils(SHA3_256).digestAsHex(individualQuestionnaireDto.getPatientId().concat(SALT));
            MedicalFile medicalFile = medicalFileRepository.getMedicalFileWith_LipidProfile_FetchTypeEAGER(patientIdSHA);

            System.out.println("++"+individualQuestionnaireDto.getSocioDemographicVariables());
            System.out.println(individualQuestionnaireDto.getSocioDemographicVariables() != null);
            if (individualQuestionnaireDto.getSocioDemographicVariables() != null)
                patient.setSocioDemographicVariables(individualQuestionnaireDto.getSocioDemographicVariables());
            else
                patient.setSocioDemographicVariables(patient.getSocioDemographicVariables());
            MedicalFileHistory medicalFileHistory = new MedicalFileHistory(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), individualQuestionnaireDto.getMedicalFileHistory());
            List<MedicalFileHistory> medicalFileHistories = medicalFile.getMedicalFileHistory();
            medicalFileHistories.add(medicalFileHistory);
            medicalFile.setMedicalFileHistory(medicalFileHistories);

            medicalFileRepository.save(medicalFile);

            patientRepository.save(patient);
            return new Response(Boolean.TRUE, null);
        } catch (Exception ex){
            LOGGER.log( Level.WARNING, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }


}
