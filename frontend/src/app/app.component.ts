import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    @if (auth.estaLogueado()) {
      <nav class="bg-slate-800 text-white px-6 py-3 flex items-center gap-4 shadow">
        <span class="font-bold text-lg">CHN · Préstamos</span>
        <span class="ml-auto text-sm text-slate-300">{{ auth.nombreUsuario() }}</span>
        <button (click)="logout()" class="bg-slate-600 hover:bg-slate-500 px-3 py-1 rounded text-sm">Salir</button>
      </nav>
    }
    <main class="p-6 max-w-6xl mx-auto">
      <router-outlet />
    </main>
  `
})
export class AppComponent {
  protected auth = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
