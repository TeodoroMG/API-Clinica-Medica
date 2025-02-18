package med.voll.api.domain.consulta;



import med.voll.api.domain.consulta.desafio.ValidadorCancelamientoDeConsulta;
import med.voll.api.domain.consulta.validaciones.ValidadorDeConsultas;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.infra.errores.ValidacionDeIntegridad;

import med.voll.api.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaDeConsultaService {


    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    List<ValidadorDeConsultas> validadores;

    @Autowired
    List<ValidadorCancelamientoDeConsulta> validadorCancelamientoDeConsultas;

    public DatosDetallesConsulta agendar(DatosAgendarConsulta datos){

        // Validar la existencia del paciente
        if (!pacienteRepository.findById(datos.idPaciente()).isPresent()){
            throw new ValidacionDeIntegridad(" Este id para el paciente no fue encontrado");
        }

        // Validar la existencia del médico, si se proporcionó un id de médico
        if(datos.idMedico()!=null && !medicoRepository.existsById(datos.idMedico())){
            throw new ValidacionDeIntegridad(" Este id para el medico no fue encontrado");
        }


        validadores.forEach(v->v.validar(datos));



        var paciente = pacienteRepository.findById(datos.idPaciente()).get();

        var medico = seleccionarMedico(datos);

        if (medico == null){
            throw new ValidacionDeIntegridad(" No existen medicos disponibles para este horario y especialidad");
        }

        var consulta = new Consulta( medico, paciente,datos.fecha());

        consultaRepository.save(consulta);

        return new DatosDetallesConsulta(consulta);

    }

    public void cancelar(DatosCancelamientoConsulta datos){
        if (!consultaRepository.existsById(datos.idConsulta())){
            throw  new ValidacionDeIntegridad("Id de la consulta informado no existe");

        }

        validadorCancelamientoDeConsultas.forEach(v -> v.validar(datos));
        var consulta = consultaRepository.getReferenceById(datos.idConsulta());
        consulta.cancelar(datos.motivo());
    }

    private Medico seleccionarMedico(DatosAgendarConsulta datos) {

        if (datos.idMedico()!=null){
            return medicoRepository.getReferenceById(datos.idMedico());
        }

        if (datos.especialidad()==null){
            throw new ValidacionDeIntegridad("debe seleccionarse una especialidad para el médico");

        }

        return medicoRepository.seleccionarMedicoConEspecialidadEnFecha(datos.especialidad(),datos.fecha());
    }
}
