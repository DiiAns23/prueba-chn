package gt.com.chn.prestamos.prestamo.repository;

import gt.com.chn.prestamos.prestamo.domain.SolicitudPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, Integer> {

    Optional<SolicitudPrestamo> findByIdAndActivoTrue(Integer id);

    /** Carga cliente y estado (to-one) para mapear el DTO sin LazyInitializationException ni N+1. */
    @Query(value = """
            SELECT s FROM SolicitudPrestamo s
            JOIN FETCH s.cliente
            JOIN FETCH s.estado
            WHERE s.cliente.id = :clienteId AND s.activo = true
            """,
            countQuery = """
            SELECT COUNT(s) FROM SolicitudPrestamo s
            WHERE s.cliente.id = :clienteId AND s.activo = true
            """)
    Page<SolicitudPrestamo> findActivasPorCliente(@Param("clienteId") Integer clienteId, Pageable pageable);

    /**
     * Soft delete masivo de las solicitudes de un cliente (CU-04), en un solo UPDATE en vez de
     * cargar y modificar cada entidad. Al ser JPQL masivo no dispara callbacks ni revisa @Version,
     * por eso se ejecuta dentro de la transacción de baja del cliente.
     */
    @Modifying
    @Query("""
            UPDATE SolicitudPrestamo s
               SET s.activo = false, s.fechaBaja = :fecha, s.usuarioBajaId = :usuarioId
             WHERE s.cliente.id = :clienteId AND s.activo = true
            """)
    void darDeBajaPorCliente(@Param("clienteId") Integer clienteId,
                             @Param("fecha") Instant fecha,
                             @Param("usuarioId") Integer usuarioId);
}
