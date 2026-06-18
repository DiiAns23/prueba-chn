import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../core/cliente.service';
import { PrestamoService } from '../../core/prestamo.service';
import { PagoService } from '../../core/pago.service';
import { AuthService } from '../../core/auth.service';
import { SeleccionService } from '../../core/seleccion.service';
import { Cliente, PagoHistorial, Prestamo, Saldo } from '../../core/models';
import { ModalComponent } from '../../shared/modal.component';
import { PaginadorComponent } from '../../shared/paginador.component';

const TAM_PAGINA = 10;

@Component({
  selector: 'app-prestamos',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, PaginadorComponent],
  template: `
    <h2 class="text-xl font-bold text-slate-800 mb-4">Préstamos aprobados</h2>

    @if (!auth.esCliente()) {
      <div class="flex items-end gap-3 mb-4 flex-wrap">
        <div>
          <label class="block text-xs text-slate-500">Cliente</label>
          <select [(ngModel)]="clienteSel" (ngModelChange)="onSelect()" class="border rounded px-3 py-2 min-w-64">
            <option [ngValue]="null">— Seleccione —</option>
            @for (c of clientes(); track c.id) {
              <option [ngValue]="c.id">{{ c.nombre }} {{ c.apellido }} ({{ c.numeroIdentificacion }})</option>
            }
          </select>
        </div>
        <span class="text-slate-400 pb-2">o buscar:</span>
        <div>
          <label class="block text-xs text-slate-500">Buscar por</label>
          <select [(ngModel)]="criterio" class="border rounded px-3 py-2">
            <option value="identificacion">N.º identificación</option>
            <option value="id">ID</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-slate-500">Valor</label>
          <input [(ngModel)]="termino" (keyup.enter)="buscar()" placeholder="Ingrese el dato" class="border rounded px-3 py-2" />
        </div>
        <button (click)="buscar()" class="bg-slate-800 text-white px-4 py-2 rounded">Buscar</button>
        @if (clienteNombre()) { <span class="text-sm text-slate-600">Cliente: <b>{{ clienteNombre() }}</b></span> }
      </div>
    }

    @if (error()) { <p class="text-red-600 mb-3">{{ error() }}</p> }

    @if (clienteSel) {
      <div class="bg-white border border-slate-200 rounded-lg overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-100 text-slate-600 text-left">
            <tr><th class="px-4 py-2">#</th><th class="px-4 py-2">Monto aprobado</th><th class="px-4 py-2">Saldo pendiente</th>
                <th class="px-4 py-2">Estado</th><th class="px-4 py-2 text-right">Acciones</th></tr>
          </thead>
          <tbody>
            @for (p of prestamos(); track p.id) {
              <tr class="border-t border-slate-100">
                <td class="px-4 py-2">{{ p.id }}</td>
                <td class="px-4 py-2">{{ p.montoAprobado | number:'1.2-2' }}</td>
                <td class="px-4 py-2 font-medium">{{ p.saldoPendiente | number:'1.2-2' }}</td>
                <td class="px-4 py-2"><span class="px-2 py-1 rounded text-xs" [class]="badge(p.estadoPago)">{{ p.estadoPago }}</span></td>
                <td class="px-4 py-2 text-right space-x-2 whitespace-nowrap">
                  @if (p.estadoPago === 'VIGENTE' && auth.tienePermiso('pagos.crear')) {
                    <button (click)="abrirPago(p)" class="text-emerald-600 hover:underline">Pagar</button>
                  }
                  @if (auth.tienePermiso('pagos.leer')) {
                    <button (click)="verMas(p)" class="text-slate-600 hover:underline">{{ detalleId() === p.id ? 'Ocultar' : 'Ver más' }}</button>
                  }
                </td>
              </tr>

              @if (detalleId() === p.id) {
                <tr class="bg-slate-50 border-t border-slate-100">
                  <td colspan="5" class="px-4 py-3">
                    <div class="grid md:grid-cols-3 gap-6 text-sm">
                      <div>
                        <h4 class="font-semibold text-slate-700 mb-1">Detalles del préstamo</h4>
                        <div><span class="text-slate-500">Propósito:</span> {{ p.proposito || '—' }}</div>
                        <div><span class="text-slate-500">Detalles:</span> {{ p.detalles || '—' }}</div>
                        <div><span class="text-slate-500">Motivo de aprobación:</span> {{ p.motivoAprobacion || '—' }}</div>
                        <div><span class="text-slate-500">Plazo:</span> {{ p.plazoMeses }} meses</div>
                        <div><span class="text-slate-500">Aprobado:</span> {{ p.fechaAprobacion | date:'short' }}</div>
                      </div>
                      <div>
                        <h4 class="font-semibold text-slate-700 mb-1">Conciliación de saldo</h4>
                        @if (saldoSel(); as s) {
                          <div><span class="text-slate-500">Monto aprobado:</span> {{ s.montoAprobado | number:'1.2-2' }}</div>
                          <div><span class="text-slate-500">Total pagado:</span> {{ s.totalPagado | number:'1.2-2' }}</div>
                          <div><span class="text-slate-500">Saldo calculado:</span> {{ s.saldoCalculado | number:'1.2-2' }}</div>
                          <div><span class="text-slate-500">Saldo materializado:</span> {{ s.saldoMaterializado | number:'1.2-2' }}</div>
                        } @else { <div class="text-slate-400">Cargando…</div> }
                      </div>
                      <div>
                        <h4 class="font-semibold text-slate-700 mb-1">Historial de pagos</h4>
                        @if (historial().length) {
                          <table class="w-full">
                            <thead class="text-slate-500 text-left"><tr><th class="pr-3">Fecha</th><th class="pr-3">Monto</th><th>Recibo</th></tr></thead>
                            <tbody>
                              @for (h of historial(); track h.id) {
                                <tr><td class="pr-3">{{ h.fechaPago | date:'short' }}</td><td class="pr-3">{{ h.monto | number:'1.2-2' }}</td><td>{{ h.numeroRecibo || '—' }}</td></tr>
                              }
                            </tbody>
                          </table>
                        } @else { <div class="text-slate-400">Sin pagos registrados</div> }
                      </div>
                    </div>
                  </td>
                </tr>
              }
            } @empty {
              <tr><td colspan="5" class="px-4 py-6 text-center text-slate-400">Sin préstamos</td></tr>
            }
          </tbody>
        </table>
      </div>
      <app-paginador [page]="page" [totalPages]="totalPages" [size]="size" (pageChange)="cargar($event)" />
    }

    @if (pagoTarget(); as p) {
      <app-modal [titulo]="'Registrar pago · préstamo #' + p.id" (cerrar)="pagoTarget.set(null)">
        <form (ngSubmit)="pagar(p)" class="grid gap-3">
          <p class="text-sm text-slate-500">Saldo pendiente: <b>{{ p.saldoPendiente | number:'1.2-2' }}</b></p>
          <label class="text-sm text-slate-600">Monto
            <input name="monto" type="number" step="0.01" [(ngModel)]="montoPago" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">N.º recibo
            <input name="recibo" [(ngModel)]="reciboPago" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <div class="flex justify-end gap-2">
            <button type="button" (click)="pagoTarget.set(null)" class="px-4 py-2 rounded border">Cancelar</button>
            <button type="submit" class="bg-slate-800 text-white px-4 py-2 rounded">Registrar pago</button>
          </div>
        </form>
      </app-modal>
    }
  `
})
export class PrestamosComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private service = inject(PrestamoService);
  private pagoService = inject(PagoService);
  private seleccion = inject(SeleccionService);
  protected auth = inject(AuthService);

  clientes = signal<Cliente[]>([]);
  prestamos = signal<Prestamo[]>([]);
  clienteSel: number | null = null;
  clienteNombre = signal('');
  criterio: 'identificacion' | 'id' = 'identificacion';
  termino = '';
  page = 0; totalPages = 0; size = TAM_PAGINA;
  error = signal('');
  pagoTarget = signal<Prestamo | null>(null);
  detalleId = signal<number | null>(null);
  saldoSel = signal<Saldo | null>(null);
  historial = signal<PagoHistorial[]>([]);
  montoPago = 0;
  reciboPago = '';

  ngOnInit(): void {
    if (this.auth.esCliente()) { this.clienteSel = this.auth.clienteId(); this.cargar(0); return; }
    this.clienteService.listar(0, 100).subscribe(p => this.clientes.set(p.content));
    const pre = this.seleccion.clienteId();
    if (pre != null) {
      this.clienteSel = pre;
      this.clienteNombre.set(this.seleccion.clienteNombre());
      this.cargar(0);
      this.seleccion.limpiar();
    }
  }

  onSelect(): void { this.clienteNombre.set(''); this.cargar(0); }

  buscar(): void {
    this.error.set('');
    if (!this.termino.trim()) return;
    const obs = this.criterio === 'id'
      ? this.clienteService.obtener(+this.termino)
      : this.clienteService.buscarPorIdentificacion(this.termino.trim());
    obs.subscribe({
      next: (c: Cliente) => { this.clienteSel = c.id; this.clienteNombre.set(`${c.nombre} ${c.apellido}`); this.cargar(0); },
      error: () => { this.clienteSel = null; this.clienteNombre.set(''); this.prestamos.set([]); this.error.set('Cliente no encontrado'); }
    });
  }

  cargar(p = this.page): void {
    this.detalleId.set(null);
    if (!this.clienteSel) { this.prestamos.set([]); return; }
    this.service.listarPorCliente(this.clienteSel, p, TAM_PAGINA).subscribe({
      next: page => {
        this.prestamos.set(page.content);
        this.page = page.number; this.totalPages = page.totalPages; this.size = page.size;
      },
      error: () => this.error.set('No se pudieron cargar los préstamos')
    });
  }

  abrirPago(p: Prestamo): void { this.pagoTarget.set(p); this.montoPago = 0; this.reciboPago = ''; }

  pagar(p: Prestamo): void {
    this.error.set('');
    this.pagoService.registrar(p.id, { monto: this.montoPago, numeroRecibo: this.reciboPago }).subscribe({
      next: () => { this.pagoTarget.set(null); this.cargar(); },
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo registrar el pago')
    });
  }

  /** Despliega/oculta el detalle: conciliación de saldo + historial de pagos (carga bajo demanda). */
  verMas(p: Prestamo): void {
    if (this.detalleId() === p.id) { this.detalleId.set(null); return; }
    this.detalleId.set(p.id);
    this.saldoSel.set(null);
    this.historial.set([]);
    this.service.saldo(p.id).subscribe({ next: s => this.saldoSel.set(s) });
    this.service.historialPagos(p.id).subscribe({ next: h => this.historial.set(h) });
  }

  badge(estado: string): string {
    switch (estado) {
      case 'PAGADO': return 'bg-emerald-100 text-emerald-700';
      case 'EN_MORA': return 'bg-red-100 text-red-700';
      default: return 'bg-blue-100 text-blue-700';
    }
  }
}
