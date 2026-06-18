package gt.com.chn.prestamos.pago.repository;

import gt.com.chn.prestamos.pago.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findAllByPrestamo_IdAndActivoTrueOrderByFechaPagoDesc(Integer prestamoId);

    @Query("""
            SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
            WHERE p.prestamo.id = :prestamoId AND p.activo = true
            """)
    BigDecimal totalPagadoPorPrestamo(@Param("prestamoId") Integer prestamoId);
}
