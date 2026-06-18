import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="min-h-[70vh] flex items-center justify-center">
      <form (ngSubmit)="ingresar()" class="bg-white shadow-lg rounded-xl p-8 w-full max-w-sm border border-slate-200">
        <h1 class="text-2xl font-bold text-slate-800 mb-1">CHN · Préstamos</h1>
        <p class="text-slate-500 text-sm mb-6">Inicia sesión para continuar</p>

        <label class="block text-sm font-medium text-slate-700 mb-1">Usuario</label>
        <input name="usuario" [(ngModel)]="usuario" required autocomplete="username"
               class="w-full border border-slate-300 rounded px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-slate-400" />

        <label class="block text-sm font-medium text-slate-700 mb-1">Contraseña</label>
        <input name="contrasena" type="password" [(ngModel)]="contrasena" required autocomplete="current-password"
               class="w-full border border-slate-300 rounded px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-slate-400" />

        @if (error()) {
          <p class="text-red-600 text-sm mb-3">{{ error() }}</p>
        }

        <button type="submit" [disabled]="cargando()"
                class="w-full bg-slate-800 hover:bg-slate-700 text-white py-2 rounded font-medium disabled:opacity-60">
          {{ cargando() ? 'Ingresando…' : 'Ingresar' }}
        </button>

        <p class="text-xs text-slate-400 mt-4">Operador por defecto: <b>operador / Operador!2026</b></p>
      </form>
    </div>
  `
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  usuario = '';
  contrasena = '';
  error = signal('');
  cargando = signal(false);

  ingresar(): void {
    this.error.set('');
    this.cargando.set(true);
    this.auth.login({ nombreUsuario: this.usuario, contrasena: this.contrasena }).subscribe({
      next: () => this.router.navigate(['/clientes']),
      error: () => { this.error.set('Credenciales inválidas'); this.cargando.set(false); }
    });
  }
}
