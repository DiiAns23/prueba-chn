package gt.com.chn.prestamos.prestamo.repository;

import gt.com.chn.prestamos.prestamo.domain.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPrestamoRepository extends JpaRepository<EstadoPrestamo, Integer> {
    Optional<EstadoPrestamo> findByCodigo(String codigo);
}
