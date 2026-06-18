import { Injectable, signal } from '@angular/core';

/**
 * Puente entre pestañas: cuando en "Clientes" se pulsa "Nueva solicitud" o "Ver préstamos",
 * se guarda aquí el cliente elegido para que la pestaña destino lo preseleccione.
 */
@Injectable({ providedIn: 'root' })
export class SeleccionService {
  clienteId = signal<number | null>(null);
  clienteNombre = signal<string>('');

  set(id: number, nombre: string): void {
    this.clienteId.set(id);
    this.clienteNombre.set(nombre);
  }
  limpiar(): void {
    this.clienteId.set(null);
    this.clienteNombre.set('');
  }
}
