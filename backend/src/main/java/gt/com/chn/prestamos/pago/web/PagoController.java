package gt.com.chn.prestamos.pago.web;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prestamos/{prestamoId}/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequierePermiso("pagos.crear")
    public PagoDtos.PagoResponse registrar(@PathVariable Integer prestamoId,
                                           @Valid @RequestBody PagoDtos.PagoRequest req) {
        return pagoService.registrar(prestamoId, req);
    }

    @GetMapping
    @RequierePermiso("pagos.leer")
    public List<PagoDtos.PagoHistorialResponse> historial(@PathVariable Integer prestamoId) {
        return pagoService.historial(prestamoId);
    }
}
