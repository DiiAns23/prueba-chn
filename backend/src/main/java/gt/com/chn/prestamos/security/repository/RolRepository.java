package gt.com.chn.prestamos.security.repository;

import gt.com.chn.prestamos.security.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByCodigo(String codigo);

    List<Rol> findByCodigoIn(Collection<String> codigos);
}
