package gt.com.chn.prestamos.security.domain;

import gt.com.chn.prestamos.common.domain.EntidadAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** NULL para operador / administrador; identifica al cliente en autoservicio. */
    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "hash_contrasena", nullable = false)
    private String hashContrasena;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion = Instant.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();
}
