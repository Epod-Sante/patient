package ca.uqtr.patient.service.questionnaire;

import ca.uqtr.patient.dto.*;
import ca.uqtr.patient.dto.Error;
import ca.uqtr.patient.entity.Patient;
import ca.uqtr.patient.entity.Questionnaire;
import ca.uqtr.patient.event.questionnaire.OnQuestionnaireSendEvent;
import ca.uqtr.patient.repository.patient.PatientRepository;
import ca.uqtr.patient.repository.questionnaire.QuestionnaireRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {
    private static final Logger LOGGER = Logger.getLogger( QuestionnaireServiceImpl.class.getName() );

    private final QuestionnaireRepository questionnaireRepository;
    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private MessageSource messageSource;

    public QuestionnaireServiceImpl(QuestionnaireRepository questionnaireRepository, PatientRepository patientRepository, ModelMapper modelMapper, ApplicationEventPublisher eventPublisher, MessageSource messageSource) {
        this.questionnaireRepository = questionnaireRepository;
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
        this.messageSource = messageSource;
    }

    @Override
    public Response addQuestionnaire(QuestionnaireDto questionnaireDto) {
        Patient patient = patientRepository.getPatientById(UUID.fromString(questionnaireDto.getPatientId()));
        if (patient == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));

        try {
            Questionnaire questionnaire = questionnaireDto.dtoToObj(modelMapper);
            questionnaire.setDate(new java.sql.Date (Calendar.getInstance().getTime().getTime()));
            questionnaire.setPatient(patient);
            questionnaireRepository.save(questionnaire);
            return new Response(questionnaireDto, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.questionnaire.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.questionnaire.message", null, Locale.US)));
        }
    }

    @Override
    public void sendQuestionnaire(PatientDto patientDto) {
        eventPublisher.publishEvent(new OnQuestionnaireSendEvent(patientDto, 1));
    }

    @Override
    public Response getQuestionnairesByPatient(String patientId) {
        Patient patient = patientRepository.getPatientById(UUID.fromString(patientId));
        if (patient == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try {
            List<Questionnaire> questionnaires = patient.getQuestionnaires();
            Type questionnaireType = new TypeToken<List<QuestionnaireDto>>() {}.getType();
            List<QuestionnaireDto> questionnaireDtoList = modelMapper.map(questionnaires, questionnaireType);

            return new Response(questionnaireDtoList, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.questionnaire.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.questionnaire.message", null, Locale.US)));
        }
    }


}
