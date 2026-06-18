package gt.com.chn.prestamos.security.repository;

import gt.com.chn.prestamos.security.domain.UsuarioPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, UsuarioPermiso.PK> {

    void deleteByUsuarioId(Integer usuarioId);
}
