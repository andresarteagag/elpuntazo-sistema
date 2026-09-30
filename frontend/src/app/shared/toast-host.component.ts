import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../core/services/notification.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-stack">
      @for (n of notifications.notifications(); track n.id) {
        <div class="toast" [class.toast--error]="n.type === 'error'" [class.toast--success]="n.type === 'success'">
          <span>{{ n.message }}</span>
          <button type="button" class="toast__close" (click)="notifications.dismiss(n.id)" aria-label="Cerrar">×</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-stack {
      position: fixed;
      top: var(--space-4);
      right: var(--space-4);
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
      max-width: min(360px, calc(100vw - 32px));
    }
    .toast {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: var(--space-3);
      background: var(--color-ink);
      color: white;
      padding: var(--space-3) var(--space-4);
      border-radius: var(--radius-sm);
      box-shadow: var(--shadow-float);
      font-size: 14px;
    }
    .toast--error { background: var(--color-danger); }
    .toast--success { background: var(--color-brand-dark); }
    .toast__close {
      background: none;
      border: none;
      color: inherit;
      font-size: 18px;
      line-height: 1;
      cursor: pointer;
      padding: 0;
    }
    @media (max-width: 640px) {
      .toast-stack { left: var(--space-4); right: var(--space-4); top: auto; bottom: var(--space-4); max-width: none; }
    }
  `],
})
export class ToastHostComponent {
  notifications = inject(NotificationService);
}
