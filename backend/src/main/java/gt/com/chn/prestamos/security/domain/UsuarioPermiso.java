package gt.com.chn.prestamos.security.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Excepción de permiso por usuario:
 * granted = true  -> otorga un permiso que el rol no concede.
 * granted = false -> revoca un permiso que el rol sí concede.
 */
@Entity
@Table(name = "usuario_permiso")
@IdClass(UsuarioPermiso.PK.class)
@Getter
@Setter
public class UsuarioPermiso {

    @Id
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Id
    @Column(name = "permiso_id")
    private Integer permisoId;

    @Column(nullable = false)
    private boolean granted;

    @Getter
    @Setter
    public static class PK implements Serializable {
        private Integer usuarioId;
        private Integer permisoId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(usuarioId, pk.usuarioId) && Objects.equals(permisoId, pk.permisoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(usuarioId, permisoId);
        }
    }
}
