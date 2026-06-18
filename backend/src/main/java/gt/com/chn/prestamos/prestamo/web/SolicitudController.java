package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequierePermiso("prestamos.crear")
    public SolicitudDtos.SolicitudResponse crear(@Valid @RequestBody SolicitudDtos.SolicitudRequest req) {
        return solicitudService.crear(req);
    }

    @GetMapping
    @RequierePermiso("prestamos.leer")
    public Page<SolicitudDtos.SolicitudResponse> listarPorCliente(
            @RequestParam Integer clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return solicitudService.listarPorCliente(clienteId, pageable);
    }

    @PostMapping("/{id}/aprobar")
    @RequierePermiso("prestamos.aprobar")
    public SolicitudDtos.SolicitudResponse aprobar(@PathVariable Integer id,
                                                   @RequestBody(required = false) SolicitudDtos.ResolucionRequest req) {
        return solicitudService.aprobar(id, req == null ? null : req.motivo());
    }

    @PostMapping("/{id}/rechazar")
    @RequierePermiso("prestamos.rechazar")
    public SolicitudDtos.SolicitudResponse rechazar(@PathVariable Integer id,
                                                    @RequestBody(required = false) SolicitudDtos.ResolucionRequest req) {
        return solicitudService.rechazar(id, req == null ? null : req.motivo());
    }
}
