import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth.service';
import { SeleccionService } from '../../core/seleccion.service';
import { Cliente } from '../../core/models';
import { ClientesComponent } from '../clientes/clientes.component';
import { SolicitudesComponent } from '../solicitudes/solicitudes.component';
import { PrestamosComponent } from '../prestamos/prestamos.component';
import { MiPerfilComponent } from '../perfil/mi-perfil.component';

type Tab = 'clientes' | 'solicitudes' | 'prestamos' | 'perfil';

/** Pantalla única con pestañas. Desde Clientes se salta a Solicitudes/Préstamos del cliente elegido. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ClientesComponent, SolicitudesComponent, PrestamosComponent, MiPerfilComponent],
  template: `
    <div class="flex gap-1 border-b border-slate-200 mb-5">
      @if (auth.tienePermiso('clientes.leer')) {
        <button (click)="tab.set('clientes')" [class]="clase('clientes')">Clientes</button>
      }
      <button (click)="tab.set('solicitudes')" [class]="clase('solicitudes')">Solicitudes</button>
      <button (click)="tab.set('prestamos')" [class]="clase('prestamos')">Préstamos</button>
      @if (auth.esCliente()) {
        <button (click)="tab.set('perfil')" [class]="clase('perfil')">Mi perfil</button>
      }
    </div>

    @switch (tab()) {
      @case ('clientes') {
        <app-clientes (abrirSolicitud)="irASolicitudes($event)" (verPrestamos)="irAPrestamos($event)" />
      }
      @case ('solicitudes') { <app-solicitudes /> }
      @case ('prestamos')   { <app-prestamos /> }
      @case ('perfil')      { <app-mi-perfil /> }
    }
  `
})
export class DashboardComponent {
  protected auth = inject(AuthService);
  private seleccion = inject(SeleccionService);

  tab = signal<Tab>(this.auth.tienePermiso('clientes.leer') ? 'clientes' : 'solicitudes');

  irASolicitudes(c: Cliente): void {
    this.seleccion.set(c.id, `${c.nombre} ${c.apellido}`);
    this.tab.set('solicitudes');
  }
  irAPrestamos(c: Cliente): void {
    this.seleccion.set(c.id, `${c.nombre} ${c.apellido}`);
    this.tab.set('prestamos');
  }

  clase(t: Tab): string {
    const base = 'px-4 py-2 text-sm font-medium -mb-px border-b-2 ';
    return base + (this.tab() === t
      ? 'border-slate-800 text-slate-800'
      : 'border-transparent text-slate-500 hover:text-slate-700');
  }
}
