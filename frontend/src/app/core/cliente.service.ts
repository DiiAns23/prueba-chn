import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { Cliente, ClienteRequest, ContactoRequest, Page } from './models';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private base = `${API_URL}/clientes`;

  listar(page = 0, size = 20): Observable<Page<Cliente>> {
    return this.http.get<Page<Cliente>>(`${this.base}?page=${page}&size=${size}`);
  }
  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.base}/${id}`);
  }
  buscarPorIdentificacion(identificacion: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.base}/buscar?identificacion=${encodeURIComponent(identificacion)}`);
  }
  crear(req: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.base, req);
  }
  actualizar(id: number, req: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.base}/${id}`, req);
  }
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // --- Autoservicio del cliente (mi-perfil) ---
  miPerfil(): Observable<Cliente> {
    return this.http.get<Cliente>(`${API_URL}/mi-perfil`);
  }
  actualizarContacto(req: ContactoRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${API_URL}/mi-perfil`, req);
  }
}
