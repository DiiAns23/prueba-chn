package gt.com.chn.prestamos.cliente.web;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequierePermiso("clientes.crear")
    public ClienteDtos.ClienteResponse crear(@Valid @RequestBody ClienteDtos.ClienteRequest req) {
        return clienteService.crear(req);
    }

    @GetMapping
    @RequierePermiso("clientes.leer")
    public Page<ClienteDtos.ClienteResponse> listar(
            @PageableDefault(size = 20, sort = {"apellido", "nombre"}) Pageable pageable) {
        return clienteService.listar(pageable);
    }

    /** Búsqueda por número de identificación (selección de cliente en la vista del operador). */
    @GetMapping("/buscar")
    @RequierePermiso("clientes.leer")
    public ClienteDtos.ClienteResponse buscar(@RequestParam String identificacion) {
        return clienteService.buscarPorIdentificacion(identificacion);
    }

    @GetMapping("/{id}")
    @RequierePermiso("clientes.leer")
    public ClienteDtos.ClienteResponse obtener(@PathVariable Integer id) {
        return clienteService.obtener(id);
    }

    @PutMapping("/{id}")
    @RequierePermiso("clientes.actualizar")
    public ClienteDtos.ClienteResponse actualizar(@PathVariable Integer id,
                                                  @Valid @RequestBody ClienteDtos.ClienteRequest req) {
        return clienteService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequierePermiso("clientes.eliminar")
    public void eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
    }
}
