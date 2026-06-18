import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../core/cliente.service';
import { Cliente, ContactoRequest } from '../../core/models';
import { ModalComponent } from '../../shared/modal.component';

@Component({
  selector: 'app-mi-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  template: `
    <div class="flex items-center mb-4">
      <h2 class="text-xl font-bold text-slate-800">Mi perfil</h2>
      @if (cliente()) {
        <button (click)="abrir()" class="ml-auto bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded">Editar contacto</button>
      }
    </div>

    @if (error()) { <p class="text-red-600 mb-3">{{ error() }}</p> }

    @if (cliente(); as c) {
      <div class="bg-white border border-slate-200 rounded-lg p-5 max-w-lg space-y-2 text-sm">
        <!-- Datos no modificables por el cliente -->
        <div><span class="text-slate-500">Nombre:</span> <b>{{ c.nombre }} {{ c.apellido }}</b></div>
        <div><span class="text-slate-500">Identificación:</span> {{ c.numeroIdentificacion }}</div>
        <div><span class="text-slate-500">Fecha de nacimiento:</span> {{ c.fechaNacimiento }}</div>
        <hr class="my-2 border-slate-100" />
        <!-- Datos de contacto (editables) -->
        <div><span class="text-slate-500">Correo:</span> {{ c.correoElectronico || '—' }}</div>
        <div><span class="text-slate-500">Teléfono:</span> {{ c.telefono || '—' }}</div>
        <div><span class="text-slate-500">Dirección:</span> {{ c.direccion || '—' }}</div>
      </div>
    }

    @if (mostrarForm()) {
      <app-modal titulo="Editar datos de contacto" (cerrar)="mostrarForm.set(false)">
        <form (ngSubmit)="guardar()" class="grid gap-3">
          <label class="text-sm text-slate-600">Correo electrónico
            <input name="correo" type="email" [(ngModel)]="form.correoElectronico" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Teléfono
            <input name="tel" [(ngModel)]="form.telefono" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <label class="text-sm text-slate-600">Dirección
            <input name="dir" [(ngModel)]="form.direccion" class="w-full border rounded px-3 py-2 mt-1" />
          </label>
          <div class="flex gap-2 justify-end mt-2">
            <button type="button" (click)="mostrarForm.set(false)" class="px-4 py-2 rounded border">Cancelar</button>
            <button type="submit" class="bg-slate-800 text-white px-4 py-2 rounded">Guardar</button>
          </div>
        </form>
      </app-modal>
    }
  `
})
export class MiPerfilComponent implements OnInit {
  private service = inject(ClienteService);

  cliente = signal<Cliente | null>(null);
  error = signal('');
  mostrarForm = signal(false);
  form: ContactoRequest = {};

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.service.miPerfil().subscribe({
      next: c => this.cliente.set(c),
      error: () => this.error.set('No se pudo cargar tu perfil')
    });
  }

  abrir(): void {
    const c = this.cliente()!;
    this.form = { correoElectronico: c.correoElectronico, telefono: c.telefono, direccion: c.direccion };
    this.mostrarForm.set(true);
  }

  guardar(): void {
    this.error.set('');
    this.service.actualizarContacto(this.form).subscribe({
      next: c => { this.cliente.set(c); this.mostrarForm.set(false); },
      error: err => this.error.set(err?.error?.detail ?? 'No se pudo actualizar el contacto')
    });
  }
}
