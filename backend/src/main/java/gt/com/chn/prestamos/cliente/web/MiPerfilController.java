package gt.com.chn.prestamos.cliente.web;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Autoservicio del cliente: consultar y actualizar sus propios datos de contacto.
 * Siempre opera sobre el cliente del token (no recibe id), así que no hay riesgo de IDOR.
 */
@RestController
@RequestMapping("/api/v1/mi-perfil")
public class MiPerfilController {

    private final ClienteService clienteService;

    public MiPerfilController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @RequierePermiso("clientes.contacto")
    public ClienteDtos.ClienteResponse miPerfil() {
        return clienteService.miPerfil();
    }

    @PutMapping
    @RequierePermiso("clientes.contacto")
    public ClienteDtos.ClienteResponse actualizarContacto(@Valid @RequestBody ClienteDtos.ContactoRequest req) {
        return clienteService.actualizarContacto(req);
    }
}
