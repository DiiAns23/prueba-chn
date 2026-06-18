package gt.com.chn.prestamos.security.repository;

import gt.com.chn.prestamos.security.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    Page<Usuario> findByActivoTrue(Pageable pageable);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByClienteId(Integer clienteId);

    /**
     * Permisos efectivos del usuario (MER §4.11):
     * permisos heredados de roles, menos los revocados por excepción (granted = 0),
     * más los otorgados por excepción individual (granted = 1).
     */
    @Query(value = """
            SELECT DISTINCT p.codigo
            FROM   permiso p
            JOIN   rol_permiso rp ON rp.permiso_id = p.id
            JOIN   usuario_rol  ur ON ur.rol_id     = rp.rol_id
            WHERE  ur.usuario_id = :usuarioId
              AND  NOT EXISTS (SELECT 1 FROM usuario_permiso up
                               WHERE up.usuario_id = :usuarioId
                                 AND up.permiso_id = p.id
                                 AND up.granted    = 0)
            UNION
            SELECT p.codigo
            FROM   permiso p
            JOIN   usuario_permiso up ON up.permiso_id = p.id
            WHERE  up.usuario_id = :usuarioId
              AND  up.granted    = 1
            """, nativeQuery = true)
    List<String> findPermisosEfectivos(@Param("usuarioId") Integer usuarioId);
}
