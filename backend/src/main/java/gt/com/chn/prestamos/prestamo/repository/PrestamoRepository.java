package gt.com.chn.prestamos.prestamo.repository;

import gt.com.chn.prestamos.prestamo.domain.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    Optional<Prestamo> findByIdAndActivoTrue(Integer id);

    /** Carga solicitud, cliente y estado (to-one) para mapear el DTO sin LazyInitialization ni N+1. */
    @Query(value = """
            SELECT p FROM Prestamo p
            JOIN FETCH p.solicitud sc
            JOIN FETCH sc.cliente
            JOIN FETCH p.estadoPago
            WHERE sc.cliente.id = :clienteId AND p.activo = true
            """,
            countQuery = """
            SELECT COUNT(p) FROM Prestamo p
            WHERE p.solicitud.cliente.id = :clienteId AND p.activo = true
            """)
    Page<Prestamo> findActivosPorCliente(@Param("clienteId") Integer clienteId, Pageable pageable);
}
