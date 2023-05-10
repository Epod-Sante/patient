package ca.uqtr.patient.service.recommendation;

import ca.uqtr.patient.dto.AppointmentDto;
import ca.uqtr.patient.dto.Error;
import ca.uqtr.patient.dto.RecommendationDto;
import ca.uqtr.patient.dto.Response;
import ca.uqtr.patient.entity.Appointment;
import ca.uqtr.patient.entity.Patient;
import ca.uqtr.patient.entity.Recommendation;
import ca.uqtr.patient.repository.patient.PatientRepository;
import ca.uqtr.patient.repository.professional.ProfessionalRepository;
import ca.uqtr.patient.repository.recommendation.RecommendationRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOGGER = Logger.getLogger( RecommendationServiceImpl.class.getName() );

    private PatientRepository patientRepository;
    private RecommendationRepository recommendationRepository;
    private ProfessionalRepository professionalRepository;
    private MessageSource messageSource;
    private final ModelMapper modelMapper;

    public RecommendationServiceImpl(PatientRepository patientRepository, RecommendationRepository recommendationRepository, ProfessionalRepository professionalRepository, MessageSource messageSource, ModelMapper modelMapper) {
        this.patientRepository = patientRepository;
        this.recommendationRepository = recommendationRepository;
        this.professionalRepository = professionalRepository;
        this.messageSource = messageSource;
        this.modelMapper = modelMapper;
    }

    @Override
    public Response addRecommendation(RecommendationDto recommendationDto) {
        Patient patient = patientRepository.getPatientById(recommendationDto.getPatient().getId());
        if (patient == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try {
            Recommendation recommendation = recommendationDto.dtoToObj(modelMapper);
            recommendation.setPatient(patient);
            recommendation.setProfessional(professionalRepository.findById(recommendationDto.getProfessional().getId()).get());
            recommendation.setDateRecommendation(new Date(Calendar.getInstance().getTimeInMillis()));
            recommendationRepository.save(recommendation);
            return new Response(recommendationDto, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.recommendation.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.recommendation.message", null, Locale.US)));
        }
    }

    @Override
    public Response getLastRecommendationByPatient(String patientId){
        Patient patient = patientRepository.getPatientById(UUID.fromString(patientId));
        if (patient == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));

        /*List<Recommendation> recommendations = patient.getRecommendations();
        Type recommendationType = new TypeToken<List<RecommendationDto>>() {}.getType();
        List<RecommendationDto> recommendationDtoList = modelMapper.map(recommendations, recommendationType);*/
        List<Recommendation> recommendations = recommendationRepository.getRecommendationByPatientAndResponseIsNull(patient);
        System.out.println("---------   "+recommendations.size());
        return new Response(modelMapper.map(recommendations.get(0), RecommendationDto.class), null);


    }

    @Override
    public Response updateRecommendationByPatient(RecommendationDto recommendationDto) {
        Patient patient = patientRepository.getPatientById(recommendationDto.getPatient().getId());
        if (patient == null)
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try {
            Recommendation recommendation = recommendationRepository.findTopByPatientInOrderByIdDescPatient(patient);
            if (recommendationDto.dtoToObj(modelMapper).getBarriersRecommendation() != null)
                recommendation.setBarriersRecommendation(recommendationDto.dtoToObj(modelMapper).getBarriersRecommendation());
            if (recommendationDto.dtoToObj(modelMapper).getConfiance() != null)
                recommendation.setConfiance(recommendationDto.dtoToObj(modelMapper).getConfiance());
            recommendationRepository.save(recommendation);
            return new Response(recommendationDto, null);
        } catch (Exception e){
            LOGGER.log( Level.WARNING, e.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.recommendation.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.recommendation.message", null, Locale.US)));
        }
    }

    @Override
    public Response getRecommendationsByPatient(String patientId) {
        Optional<Patient> patient = patientRepository.findById(UUID.fromString(patientId));
        if (!patient.isPresent())
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try{
            Type recommendationDtoList = new TypeToken<List<RecommendationDto>>() {}.getType();
            List<Recommendation> recommendations = patient.get().getRecommendations();
            return new Response(modelMapper.map(recommendations, recommendationDtoList), null);
        } catch (Exception ex){
            LOGGER.log( Level.ALL, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }


}
