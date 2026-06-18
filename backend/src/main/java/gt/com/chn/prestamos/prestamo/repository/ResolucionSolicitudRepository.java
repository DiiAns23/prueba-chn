package gt.com.chn.prestamos.prestamo.repository;

import gt.com.chn.prestamos.prestamo.domain.ResolucionSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ResolucionSolicitudRepository extends JpaRepository<ResolucionSolicitud, Integer> {

    /** Resoluciones de un conjunto de solicitudes (para traer el motivo en bloque, sin N+1). */
    List<ResolucionSolicitud> findBySolicitudIdIn(Collection<Integer> solicitudIds);
}
