import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { Pago, PagoRequest } from './models';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private http = inject(HttpClient);

  registrar(prestamoId: number, req: PagoRequest): Observable<Pago> {
    return this.http.post<Pago>(`${API_URL}/prestamos/${prestamoId}/pagos`, req);
  }
}
