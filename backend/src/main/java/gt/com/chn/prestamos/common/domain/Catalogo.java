package gt.com.chn.prestamos.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base para catálogos simples (código + descripción): EstadoSolicitud, EstadoPrestamo,
 * MetodoPago, Modulo, y reutilizada por Permiso/Rol. Abstracción + herencia.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String descripcion;
}
