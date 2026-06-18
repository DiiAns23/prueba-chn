import { Component, EventEmitter, Input, Output } from '@angular/core';

/** Paginación mínima: página actual, total de páginas y tamaño de página. */
@Component({
  selector: 'app-paginador',
  standalone: true,
  template: `
    @if (totalPages > 0) {
      <div class="flex items-center gap-3 mt-3 text-sm text-slate-600">
        <button (click)="ir(page - 1)" [disabled]="page <= 0"
                class="px-3 py-1 border rounded disabled:opacity-40">‹ Anterior</button>
        <span>Página {{ page + 1 }} de {{ totalPages }} · {{ size }} por página</span>
        <button (click)="ir(page + 1)" [disabled]="page >= totalPages - 1"
                class="px-3 py-1 border rounded disabled:opacity-40">Siguiente ›</button>
      </div>
    }
  `
})
export class PaginadorComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() size = 0;
  @Output() pageChange = new EventEmitter<number>();

  ir(p: number): void {
    if (p >= 0 && p < this.totalPages) this.pageChange.emit(p);
  }
}
