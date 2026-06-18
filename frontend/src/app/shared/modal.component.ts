import { Component, EventEmitter, Input, Output } from '@angular/core';

/** Modal reutilizable: overlay + tarjeta centrada. El contenido se proyecta con <ng-content>. */
@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    <div class="fixed inset-0 bg-black/40 flex items-start justify-center z-50 p-4 overflow-auto"
         (click)="cerrar.emit()">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mt-16" (click)="$event.stopPropagation()">
        <div class="flex items-center px-5 py-3 border-b border-slate-200">
          <h3 class="font-semibold text-slate-800">{{ titulo }}</h3>
          <button class="ml-auto text-slate-400 hover:text-slate-700 text-xl leading-none" (click)="cerrar.emit()">×</button>
        </div>
        <div class="p-5">
          <ng-content />
        </div>
      </div>
    </div>
  `
})
export class ModalComponent {
  @Input() titulo = '';
  @Output() cerrar = new EventEmitter<void>();
}
