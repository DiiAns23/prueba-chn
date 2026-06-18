import { Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../core/cliente.service';
import { AuthService } from '../../core/auth.service';
import { Cliente, ClienteRequest } from '../../core/models';
import { ModalComponent } from '../../shared/modal.component';
import { PaginadorComponent } from '../../shared/paginador.component';

const TAM_PAGINA = 10;

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, PaginadorComponent],
  template: `
    <div class="flex items-center mb-4">
      <h2 class="text-xl font-bold text-slate-800">Clientes</h2>
      @if (auth.tienePermiso('clientes.crear')) {
        <button (click)="nuevo()" class="ml-auto bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded">+ Nuevo</button>
      }
    </div>

    @if (error()) { <p class="text-red-600 mb-3">{{ error() }}</p> }

    <div class="bg-white border border-slate-200 rounded-lg overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-100 text-slate-600 text-left">
          <tr>
            <th class="px-4 py-2">Nombre</th><th class="px-4 py-2">Identificación</th>
            <th class="px-4 py-2">Correo</th><th class="px-4 py-2">Teléfono</th>
            <th class="px-4 py-2 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (c of clientes(); track c.id) {
            <tr class="border-t border-slate-100">
              <td class="px-4 py-2">{{ c.nombre }} {{ c.apellido }}</td>
              <td class="px-4 py-2">{{ c.numeroIdentificacion }}</td>
              <td class="px-4 py-2">{{ c.correoElectronico }}</td>
              <td class="px-4 py-2">{{ c.telefono }}</td>
              <td class="px-4 py-2 text-right space-x-2 whitespace-nowrap">
                @if (auth.tienePermiso('prestamos.crear')) {
                  <button (click)="abrirSolicitud.emit(c)" class="text-emerald-600 hover:underline">Nueva solicitud</button>
                }
                @if (auth.tienePermiso('prestamos.leer')) {
                  <button (click)="verPrestamos.emit(c)" class="text-blue-600 hover:underline">Ver préstamos</button>
                }
                @if (auth.tienePermiso('clientes.actualizar')) {
                  <button (click)="editar(c)" class="text-slate-600 hover:underline">Editar</button>
                }
                @if (auth.tienePermiso('clientes.eliminar')) {
                  <button (click)="eliminar(c)" class="text-red-600 hover:underline">Eliminar</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="5" class="px-4 py-6 text-center text-slate-400">Sin clientes</td></tr>
          }
        </tbody>
      </table>
    </div>
    <app-paginador [page]="page" [totalPages]="totalPages" [size]="size" (pageChange)="cargar($event)" />

    @if (mostrarForm()) {
      <app-modal [titulo]="editId() ? 'Editar cliente' : 'Nuevo cliente'" (cerrar)="cancelar()">
        <form (ngSubmit)="guardar()" class="grid grid-cols-2 gap-3">
          <label class="text-sm text-slate-600">Nombre
            <input name="nombre" [(ngModel)]="form.nombre" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Apellido
            <input name="apellido" [(ngModel)]="form.apellido" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">N.º identificación
            <input name="ident" [(ngModel)]="form.numeroIdentificacion" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Fecha de nacimiento
            <input name="nac" type="date" [(ngModel)]="form.fechaNacimiento" required class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Correo electrónico
            <input name="correo" type="email" [(ngModel)]="form.correoElectronico" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Teléfono
            <input name="tel" [(ngModel)]="form.telefono" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600 col-span-2">Dirección
            <input name="dir" [(ngModel)]="form.direccion" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <div class="col-span-2 flex gap-2 justify-end">
            <button type="button" (click)="cancelar()" class="px-4 py-2 rounded border">Cancelar</button>
            <button type="submit" class="bg-slate-800 text-white px-4 py-2 rounded">Guardar</button>
          </div>
        </form>
      </app-modal>
    }
  `
})
export class ClientesComponent implements OnInit {
  private service = inject(ClienteService);
  protected auth = inject(AuthService);

  // El dashboard escucha estos eventos para saltar a la pestaña con el cliente preseleccionado.
  @Output() abrirSolicitud = new EventEmitter<Cliente>();
  @Output() verPrestamos = new EventEmitter<Cliente>();

  clientes = signal<Cliente[]>([]);
  page = 0; totalPages = 0; size = TAM_PAGINA;
  error = signal('');
  mostrarForm = signal(false);
  editId = signal<number | null>(null);
  form: ClienteRequest = this.vacio();

  ngOnInit(): void { this.cargar(); }

  cargar(p = this.page): void {
    this.service.listar(p, TAM_PAGINA).subscribe({
      next: page => {
        this.clientes.set(page.content);
        this.page = page.number; this.totalPages = page.totalPages; this.size = page.size;
      },
      error: () => this.error.set('No se pudo cargar la lista de clientes')
    });
  }

  nuevo(): void { this.editId.set(null); this.form = this.vacio(); this.mostrarForm.set(true); }

  editar(c: Cliente): void {
    this.editId.set(c.id);
    this.form = { nombre: c.nombre, apellido: c.apellido, numeroIdentificacion: c.numeroIdentificacion,
      fechaNacimiento: c.fechaNacimiento, direccion: c.direccion, correoElectronico: c.correoElectronico, telefono: c.telefono };
    this.mostrarForm.set(true);
  }

  guardar(): void {
    this.error.set('');
    const id = this.editId();
    const obs = id ? this.service.actualizar(id, this.form) : this.service.crear(this.form);
    obs.subscribe({
      next: () => { this.mostrarForm.set(false); this.cargar(); },
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo guardar el cliente')
    });
  }

  eliminar(c: Cliente): void {
    if (!confirm(`¿Dar de baja a ${c.nombre} ${c.apellido}? (soft delete)`)) return;
    this.service.eliminar(c.id).subscribe({
      next: () => this.cargar(),
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo eliminar')
    });
  }

  cancelar(): void { this.mostrarForm.set(false); }

  private vacio(): ClienteRequest {
    return { nombre: '', apellido: '', numeroIdentificacion: '', fechaNacimiento: '', direccion: '', correoElectronico: '', telefono: '' };
  }
}
