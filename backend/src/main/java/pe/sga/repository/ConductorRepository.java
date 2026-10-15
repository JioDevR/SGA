package pe.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sga.entity.Conductor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Integer> {

    Optional<Conductor> findByNumeroLicencia(String numeroLicencia);

    boolean existsByNumeroLicencia(String numeroLicencia);

    List<Conductor> findByEstadoActivo(Boolean estadoActivo);
}
