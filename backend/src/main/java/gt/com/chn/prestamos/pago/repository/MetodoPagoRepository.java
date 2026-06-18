package gt.com.chn.prestamos.pago.repository;

import gt.com.chn.prestamos.pago.domain.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByCodigo(String codigo);
}
