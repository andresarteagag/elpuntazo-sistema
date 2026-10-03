import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../core/services/invoice.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Invoice, InvoiceStatus } from '../../core/models/invoice.model';
import { CopCurrencyPipe } from '../../shared/cop-currency.pipe';

@Component({
  selector: 'app-historial',
  standalone: true,
  imports: [CommonModule, FormsModule, CopCurrencyPipe],
  templateUrl: './historial.component.html',
  styleUrl: './historial.component.css',
})
export class HistorialComponent {
  private invoiceService = inject(InvoiceService);
  private notifications = inject(NotificationService);
  auth = inject(AuthService);

  invoices = signal<Invoice[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  statusFilter = signal<InvoiceStatus | ''>('');
  fromFilter = signal('');
  toFilter = signal('');
  numberFilter = signal('');

  invoiceToCancel = signal<Invoice | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.invoiceService
      .search({
        status: this.statusFilter() || undefined,
        from: this.fromFilter() || undefined,
        to: this.toFilter() || undefined,
        invoiceNumber: this.numberFilter() || undefined,
        page: this.page(),
      })
      .subscribe({
        next: (result) => {
          this.invoices.set(result.content);
          this.totalPages.set(result.totalPages);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  applyFilters(): void {
    this.page.set(0);
    this.load();
  }

  changePage(delta: number): void {
    const next = this.page() + delta;
    if (next < 0 || next >= this.totalPages()) return;
    this.page.set(next);
    this.load();
  }

  download(invoice: Invoice): void {
    this.invoiceService.downloadPdf(invoice.id, invoice.invoiceNumber);
  }

  askCancel(invoice: Invoice): void {
    this.invoiceToCancel.set(invoice);
  }

  confirmCancel(): void {
    const invoice = this.invoiceToCancel();
    if (!invoice) return;

    this.invoiceService.cancel(invoice.id).subscribe({
      next: () => {
        this.notifications.show('Factura anulada correctamente.', 'success');
        this.invoiceToCancel.set(null);
        this.load();
      },
      // Sin esto, al fallar la anulacion la ventana de confirmacion se
      // quedaba abierta y el usuario no entendia que habia pasado.
      error: () => this.invoiceToCancel.set(null),
    });
  }

  dismissCancel(): void {
    this.invoiceToCancel.set(null);
  }
}
