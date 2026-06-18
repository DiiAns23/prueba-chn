package gt.com.chn.prestamos.prestamo.domain;

import gt.com.chn.prestamos.common.domain.Catalogo;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado_solicitud")
public class EstadoSolicitud extends Catalogo {

    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String APROBADO = "APROBADO";
    public static final String RECHAZADO = "RECHAZADO";
}
