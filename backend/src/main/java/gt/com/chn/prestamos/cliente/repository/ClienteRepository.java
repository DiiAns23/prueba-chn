package gt.com.chn.prestamos.cliente.repository;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Page<Cliente> findByActivoTrue(Pageable pageable);

    Optional<Cliente> findByIdAndActivoTrue(Integer id);

    Optional<Cliente> findByNumeroIdentificacionAndActivoTrue(String numeroIdentificacion);

    boolean existsByNumeroIdentificacionAndActivoTrue(String numeroIdentificacion);

    /** Para validar unicidad al actualizar, excluyendo al propio cliente. */
    boolean existsByNumeroIdentificacionAndActivoTrueAndIdNot(String numeroIdentificacion, Integer id);
}
