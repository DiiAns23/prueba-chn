import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../core/cliente.service';
import { SolicitudService } from '../../core/solicitud.service';
import { AuthService } from '../../core/auth.service';
import { SeleccionService } from '../../core/seleccion.service';
import { Cliente, Solicitud, SolicitudRequest } from '../../core/models';
import { ModalComponent } from '../../shared/modal.component';
import { PaginadorComponent } from '../../shared/paginador.component';

const TAM_PAGINA = 10;

@Component({
  selector: 'app-solicitudes',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, PaginadorComponent],
  template: `
    <div class="flex items-center mb-4">
      <h2 class="text-xl font-bold text-slate-800">Solicitudes de préstamo</h2>
      @if (auth.tienePermiso('prestamos.crear')) {
        <button (click)="abrirForm()" [disabled]="!clienteSel"
                class="ml-auto bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed text-white px-4 py-2 rounded">
          + Nueva solicitud
        </button>
      }
    </div>

    <!-- Operador: dropdown del padrón (ver por cada cliente) + búsqueda por id/identificación. -->
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
            <tr><th class="px-4 py-2">#</th><th class="px-4 py-2">Monto</th><th class="px-4 py-2">Plazo</th>
                <th class="px-4 py-2">Estado</th><th class="px-4 py-2">Motivo</th><th class="px-4 py-2">Fecha</th><th class="px-4 py-2 text-right">Acciones</th></tr>
          </thead>
          <tbody>
            @for (s of solicitudes(); track s.id) {
              <tr class="border-t border-slate-100">
                <td class="px-4 py-2">{{ s.id }}</td>
                <td class="px-4 py-2">{{ s.montoSolicitado | number:'1.2-2' }}</td>
                <td class="px-4 py-2">{{ s.plazoMeses }} m</td>
                <td class="px-4 py-2"><span class="px-2 py-1 rounded text-xs" [class]="badge(s.estado)">{{ s.estado }}</span></td>
                <td class="px-4 py-2 text-slate-500 max-w-xs truncate" [title]="s.motivo">{{ s.motivo || '—' }}</td>
                <td class="px-4 py-2">{{ s.fechaSolicitud | date:'short' }}</td>
                <td class="px-4 py-2 text-right space-x-2">
                  @if (s.estado === 'EN_PROCESO') {
                    @if (auth.tienePermiso('prestamos.aprobar')) { <button (click)="resolver(s, true)" class="text-emerald-600 hover:underline">Aprobar</button> }
                    @if (auth.tienePermiso('prestamos.rechazar')) { <button (click)="resolver(s, false)" class="text-red-600 hover:underline">Rechazar</button> }
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="px-4 py-6 text-center text-slate-400">Sin solicitudes</td></tr>
            }
          </tbody>
        </table>
      </div>
      <app-paginador [page]="page" [totalPages]="totalPages" [size]="size" (pageChange)="cargar($event)" />
    }

    @if (mostrarForm() && clienteSel) {
      <app-modal titulo="Nueva solicitud" (cerrar)="mostrarForm.set(false)">
        <form (ngSubmit)="crear()" class="grid grid-cols-2 gap-3">
          <label class="text-sm text-slate-600">Monto solicitado
            <input name="monto" type="number" step="0.01" [(ngModel)]="form.montoSolicitado" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Plazo (meses)
            <input name="plazo" type="number" [(ngModel)]="form.plazoMeses" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600 col-span-2">Propósito
            <input name="prop" [(ngModel)]="form.proposito" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600 col-span-2">Detalles
            <textarea name="det" [(ngModel)]="form.detalles" rows="2" class="w-full border rounded px-3 py-2 mt-1"></textarea>
          </label>
          <div class="col-span-2 flex gap-2 justify-end">
            <button type="button" (click)="mostrarForm.set(false)" class="px-4 py-2 rounded border">Cancelar</button>
            <button type="submit" class="bg-slate-800 text-white px-4 py-2 rounded">Solicitar</button>
          </div>
        </form>
      </app-modal>
    }
  `
})
export class SolicitudesComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private service = inject(SolicitudService);
  private seleccion = inject(SeleccionService);
  protected auth = inject(AuthService);

  clientes = signal<Cliente[]>([]);
  solicitudes = signal<Solicitud[]>([]);
  clienteSel: number | null = null;
  clienteNombre = signal('');
  criterio: 'identificacion' | 'id' = 'identificacion';
  termino = '';
  page = 0; totalPages = 0; size = TAM_PAGINA;
  error = signal('');
  mostrarForm = signal(false);
  form: SolicitudRequest = this.vacio();

  ngOnInit(): void {
    if (this.auth.esCliente()) { this.clienteSel = this.auth.clienteId(); this.cargar(0); return; }
    this.clienteService.listar(0, 100).subscribe(p => this.clientes.set(p.content));
    // Si se llegó desde "Clientes" con un cliente elegido, preseleccionarlo.
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
      error: () => { this.clienteSel = null; this.clienteNombre.set(''); this.solicitudes.set([]); this.error.set('Cliente no encontrado'); }
    });
  }

  cargar(p = this.page): void {
    if (!this.clienteSel) { this.solicitudes.set([]); return; }
    this.service.listarPorCliente(this.clienteSel, p, TAM_PAGINA).subscribe({
      next: page => {
        this.solicitudes.set(page.content);
        this.page = page.number; this.totalPages = page.totalPages; this.size = page.size;
      },
      error: () => this.error.set('No se pudieron cargar las solicitudes')
    });
  }

  abrirForm(): void { this.form = this.vacio(); this.mostrarForm.set(true); }

  crear(): void {
    this.error.set('');
    this.form.clienteId = this.clienteSel!;
    this.service.crear(this.form).subscribe({
      next: () => { this.mostrarForm.set(false); this.cargar(0); },
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo crear la solicitud')
    });
  }

  resolver(s: Solicitud, aprobar: boolean): void {
    const motivo = prompt(`Motivo de ${aprobar ? 'aprobación' : 'rechazo'}:`) ?? '';
    const obs = aprobar ? this.service.aprobar(s.id, motivo) : this.service.rechazar(s.id, motivo);
    obs.subscribe({
      next: () => this.cargar(),
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo resolver la solicitud')
    });
  }

  badge(estado: string): string {
    switch (estado) {
      case 'APROBADO': return 'bg-emerald-100 text-emerald-700';
      case 'RECHAZADO': return 'bg-red-100 text-red-700';
      default: return 'bg-amber-100 text-amber-700';
    }
  }

  private vacio(): SolicitudRequest {
    return { clienteId: 0, montoSolicitado: 0, plazoMeses: 0, proposito: '' };
  }
}
