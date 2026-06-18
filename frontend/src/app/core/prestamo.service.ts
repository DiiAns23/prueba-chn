import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { Page, PagoHistorial, Prestamo, Saldo } from './models';

@Injectable({ providedIn: 'root' })
export class PrestamoService {
  private http = inject(HttpClient);
  private base = `${API_URL}/prestamos`;

  listarPorCliente(clienteId: number, page = 0, size = 20): Observable<Page<Prestamo>> {
    return this.http.get<Page<Prestamo>>(`${this.base}?clienteId=${clienteId}&page=${page}&size=${size}`);
  }
  saldo(id: number): Observable<Saldo> {
    return this.http.get<Saldo>(`${this.base}/${id}/saldo`);
  }
  historialPagos(id: number): Observable<PagoHistorial[]> {
    return this.http.get<PagoHistorial[]>(`${this.base}/${id}/pagos`);
  }
}
