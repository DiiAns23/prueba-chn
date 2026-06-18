package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    @RequierePermiso("prestamos.leer")
    public Page<PrestamoDtos.PrestamoResponse> listarPorCliente(
            @RequestParam Integer clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return prestamoService.listarPorCliente(clienteId, pageable);
    }

    @GetMapping("/{id}/saldo")
    @RequierePermiso("pagos.leer")
    public PrestamoDtos.SaldoResponse saldo(@PathVariable Integer id) {
        return prestamoService.saldo(id);
    }
}
