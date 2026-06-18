package gt.com.chn.prestamos.prestamo.repository;

import gt.com.chn.prestamos.prestamo.domain.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Integer> {
    Optional<EstadoSolicitud> findByCodigo(String codigo);
}
