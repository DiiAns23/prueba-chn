package gt.com.chn.prestamos.security.domain;

import gt.com.chn.prestamos.common.domain.Catalogo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permiso")
@Getter
@Setter
public class Permiso extends Catalogo {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false)
    private String accion;
}
