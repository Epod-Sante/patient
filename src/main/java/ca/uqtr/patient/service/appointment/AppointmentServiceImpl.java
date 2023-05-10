package ca.uqtr.patient.service.appointment;

import ca.uqtr.patient.dto.AppointmentDto;
import ca.uqtr.patient.dto.Response;
import ca.uqtr.patient.dto.Error;
import ca.uqtr.patient.entity.Appointment;
import ca.uqtr.patient.entity.Patient;
import ca.uqtr.patient.repository.AppointmentRepository;
import ca.uqtr.patient.repository.patient.PatientRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AppointmentServiceImpl implements AppointmentService{
    private static final Logger LOGGER = Logger.getLogger( AppointmentServiceImpl.class.getName() );

    private AppointmentRepository appointmentRepository;
    private MessageSource messageSource;
    private PatientRepository patientRepository;
    private ModelMapper modelMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, MessageSource messageSource, PatientRepository patientRepository, ModelMapper modelMapper) {
        this.appointmentRepository = appointmentRepository;
        this.messageSource = messageSource;
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Response addAppointment(AppointmentDto appointmentDto){
        Optional<Patient> patient = patientRepository.findById(appointmentDto.getPatientId());
        if (patient.isEmpty())
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try{
            Type appointmentDtoList = new TypeToken<List<AppointmentDto>>() {}.getType();
            List<Appointment> appointments = patient.get().getAppointments();
            Appointment appointment = appointmentDto.dtoToObj(modelMapper);
            appointments.add(appointment);
            patientRepository.save(patient.get());
            return new Response(modelMapper.map(appointments, appointmentDtoList), null);
        } catch (Exception ex){
            LOGGER.log( Level.ALL, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }

    }

    @Override
    public Response getAppointments(String patientId) {
        Optional<Patient> patient = patientRepository.findById(UUID.fromString(patientId));
        if (patient.isEmpty())
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.patient.exist.id", null, Locale.US)),
                            messageSource.getMessage("error.patient.exist.message", null, Locale.US)));
        try{
            Type appointmentDtoList = new TypeToken<List<AppointmentDto>>() {}.getType();
            List<Appointment> appointments = patient.get().getAppointments();
            return new Response(modelMapper.map(appointments, appointmentDtoList), null);
        } catch (Exception ex){
            LOGGER.log( Level.ALL, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public Response updateAppointment(AppointmentDto appointmentDto){
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentDto.getId());
        if (appointment.isEmpty())
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.appointment.id", null, Locale.US)),
                            messageSource.getMessage("error.appointment.message", null, Locale.US)));
        try{
            appointment.get().setAppointmentDate(appointmentDto.getAppointmentDate());
            return new Response(modelMapper.map(appointmentRepository.save(appointment.get()), AppointmentDto.class), null);
        } catch (Exception ex){
            LOGGER.log( Level.ALL, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }
    }

    @Override
    public Response deleteAppointment(AppointmentDto appointmentDto){
        try{
            appointmentRepository.deleteById(appointmentDto.getId());
            return new Response(appointmentDto, null);
        } catch (Exception ex){
            LOGGER.log( Level.ALL, ex.getMessage());
            return new Response(null,
                    new Error(Integer.parseInt(messageSource.getMessage("error.null.id", null, Locale.US)),
                            messageSource.getMessage("error.null.message", null, Locale.US)));
        }

    }
}
