package gt.com.chn.prestamos.prestamo.domain;

import gt.com.chn.prestamos.common.domain.Catalogo;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado_prestamo")
public class EstadoPrestamo extends Catalogo {

    public static final String VIGENTE = "VIGENTE";
    public static final String PAGADO = "PAGADO";
    public static final String EN_MORA = "EN_MORA";
}
