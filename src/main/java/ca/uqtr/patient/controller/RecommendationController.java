package ca.uqtr.patient.controller;

import ca.uqtr.patient.dto.*;
import ca.uqtr.patient.dto.patient.ProfessionalDto;
import ca.uqtr.patient.service.recommendation.RecommendationService;
import ca.uqtr.patient.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class RecommendationController {

    private ObjectMapper mapper;
    private ModelMapper modelMapper;
    private RecommendationService recommendationService;

    public RecommendationController(ModelMapper modelMapper, RecommendationService recommendationService, ObjectMapper mapper) {
        this.modelMapper = modelMapper;
        this.recommendationService = recommendationService;
        this.mapper = mapper;
    }

    @PostMapping(value = "/recommendation")
    @ResponseBody
    public Response addRecommendation(@RequestBody Request request, HttpServletRequest httpRequest){
        String token = httpRequest.getHeader("Authorization").replace("bearer ","");
        RecommendationDto recommendationDto = modelMapper.map(request.getObject(), RecommendationDto.class);

/*
        recommendationDto.setProfessional(new ProfessionalDto(JwtTokenUtil.getId(token)));
        recommendationDto.setPatient(new PatientDto(JwtTokenUtil.getId(token)));
*/
        System.out.println("----------  "+recommendationDto.toString());
        recommendationDto.setProfessional(new ProfessionalDto(JwtTokenUtil.getId(token)));
        return recommendationService.addRecommendation(recommendationDto);
    }

    @PutMapping(value = "/recommendation")
    @ResponseBody
    public Response updateRecommendation(@RequestBody Request request){
        System.out.println("-----------------"+request.getObject().toString());
        RecommendationDto recommendationDto = mapper.convertValue(request.getObject(), RecommendationDto.class);

        System.out.println("-----------------"+recommendationDto);
        return recommendationService.updateRecommendationByPatient(recommendationDto);
    }

    @GetMapping(value = "/recommendation")
    @ResponseBody
    public Response getLastRecommendationByPatient(@RequestParam String patientId)  {
        return recommendationService.getLastRecommendationByPatient(patientId);
    }

    @GetMapping(value = "/recommendation/all")
    @ResponseBody
    public Response getRecommendationsByPatient(@RequestParam String patientId){
        return recommendationService.getRecommendationsByPatient(patientId);
    }

}
