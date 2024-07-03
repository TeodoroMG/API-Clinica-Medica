package med.voll.api.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PacienteRepository extends JpaRepository<Paciente,Long> {



    @Query("""
            select p.activo
            from Paciente p
            where p.id =: idPaciente
            
            """)
    Boolean findActivoById(Long idPaciente);
}
