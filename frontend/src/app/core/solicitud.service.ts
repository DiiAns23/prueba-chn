import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { Page, Solicitud, SolicitudRequest } from './models';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private http = inject(HttpClient);
  private base = `${API_URL}/solicitudes`;

  listarPorCliente(clienteId: number, page = 0, size = 20): Observable<Page<Solicitud>> {
    return this.http.get<Page<Solicitud>>(`${this.base}?clienteId=${clienteId}&page=${page}&size=${size}`);
  }
  crear(req: SolicitudRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(this.base, req);
  }
  aprobar(id: number, motivo: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/aprobar`, { motivo });
  }
  rechazar(id: number, motivo: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/rechazar`, { motivo });
  }
}
