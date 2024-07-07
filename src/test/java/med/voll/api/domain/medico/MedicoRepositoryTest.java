package med.voll.api.domain.medico;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.direccion.DatosDireccion;
import med.voll.api.paciente.DatosRegistroPaciente;
import med.voll.api.paciente.Paciente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

//prueba de base de daros externa sin borrar nuestra propia base de datos
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MedicoRepositoryTest {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Debería retornar nulo cuando el medico se encuentra en consulta con otro paciente en ese horario")
    void seleccionarMedicoConEspecialidadEnFechaEscenario1() {
        var proximoLunes10H = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // Llamada ajustada con LocalDateTime
        var medico = registrarMedico("José", "jose@voll.med", "123456", Especialidad.CARDIOLOGIA);
        var paciente = registrarPaciente("Antonio", "paciente@mail.com", "00000000000");
        registrarConsulta(medico, paciente, proximoLunes10H);

        var medicoLibre = medicoRepository.seleccionarMedicoConEspecialidadEnFecha(Especialidad.CARDIOLOGIA, proximoLunes10H);
        assertThat(medicoLibre).isNull();
    }

    @Test
    @DisplayName("Debería retornar un medico cuando realice la consulta en la base de datos para ese horario")
    void seleccionarMedicoConEspecialidadEnFechaEscenario2() {
        var proximoLunes10H = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(10, 0);

        // Registrar un medico
        var medico = registrarMedico("José", "jose@mail.com", "123456", Especialidad.CARDIOLOGIA);

        // Intentar seleccionar un medico
        var medicoLibre = medicoRepository.seleccionarMedicoConEspecialidadEnFecha(Especialidad.CARDIOLOGIA, proximoLunes10H);
        assertThat(medicoLibre).isEqualTo(medico);
    }

    private void registrarConsulta(Medico medico, Paciente paciente, LocalDateTime fecha) {
        em.persist(new Consulta(null, medico, paciente, fecha, null));
    }

    private Medico registrarMedico(String nombre, String email, String documento, Especialidad especialidad) {
        var medico = new Medico(datosMedico(nombre, email, documento, especialidad));
        em.persist(medico);
        return medico;
    }

    private Paciente registrarPaciente(String nombre, String email, String documento) {
        var paciente = new Paciente(datosPaciente(nombre, email, documento));
        em.persist(paciente);
        return paciente;
    }

    private DatosRegistroMedico datosMedico(String nombre, String email, String documento, Especialidad especialidad) {
        return new DatosRegistroMedico(nombre, email, "61999999999", documento, especialidad, datosDireccion());
    }

    private DatosRegistroPaciente datosPaciente(String nombre, String email, String documento) {
        return new DatosRegistroPaciente(nombre, email, "619999999999", documento, datosDireccion());
    }

    private DatosDireccion datosDireccion() {
        return new DatosDireccion("Loca", "Azul", "Acapulco", "210", "12");
    }
}
