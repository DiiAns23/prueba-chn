import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_URL } from './api.config';
import { LoginRequest, LoginResponse } from './models';

const STORAGE_KEY = 'chn.sesion';

/**
 * Maneja la sesión: login, persistencia del JWT y los permisos efectivos, y consultas de
 * autorización en el cliente. Nota: el frontend usa los permisos solo para mostrar/ocultar la UI;
 * la autoridad real la tiene el backend en cada petición.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  // Estado reactivo de la sesión (signal) para que la UI reaccione al login/logout.
  private sesion = signal<LoginResponse | null>(this.leerSesion());

  readonly estaLogueado = computed(() => this.sesion() !== null);
  readonly nombreUsuario = computed(() => this.sesion()?.nombreUsuario ?? '');
  readonly clienteId = computed(() => this.sesion()?.clienteId ?? null);
  readonly esCliente = computed(() => this.sesion()?.clienteId != null);

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, req)
      .pipe(tap(resp => this.guardarSesion(resp)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sesion.set(null);
  }

  token(): string | null {
    return this.sesion()?.token ?? null;
  }

  /** Permiso efectivo (mismo código modulo.accion que evalúa @RequierePermiso en el backend). */
  tienePermiso(codigo: string): boolean {
    return this.sesion()?.permisos?.includes(codigo) ?? false;
  }

  private guardarSesion(resp: LoginResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(resp));
    this.sesion.set(resp);
  }

  private leerSesion(): LoginResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) as LoginResponse : null;
  }
}
