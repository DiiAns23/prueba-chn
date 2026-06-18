package gt.com.chn.prestamos.security.domain;

import gt.com.chn.prestamos.common.domain.Catalogo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol extends Catalogo {

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permiso> permisos = new HashSet<>();
}
