package gt.com.chn.prestamos.pago.domain;

import gt.com.chn.prestamos.common.domain.Catalogo;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "metodo_pago")
public class MetodoPago extends Catalogo {

    public static final String EFECTIVO = "EFECTIVO";
}
