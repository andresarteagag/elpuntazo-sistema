import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Invoice, InvoiceFilters, InvoiceRequest } from '../models/invoice.model';
import { PageResult } from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly baseUrl = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  create(request: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.baseUrl, request);
  }

  getById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.baseUrl}/${id}`);
  }

  search(filters: InvoiceFilters): Observable<PageResult<Invoice>> {
    const params: Record<string, string | number> = {};
    if (filters.sellerId) params['sellerId'] = filters.sellerId;
    if (filters.invoiceNumber) params['invoiceNumber'] = filters.invoiceNumber;
    if (filters.status) params['status'] = filters.status;
    if (filters.from) params['from'] = filters.from;
    if (filters.to) params['to'] = filters.to;
    params['page'] = filters.page ?? 0;
    params['size'] = filters.size ?? 20;

    return this.http.get<PageResult<Invoice>>(this.baseUrl, { params });
  }

  cancel(id: number): Observable<Invoice> {
    return this.http.patch<Invoice>(`${this.baseUrl}/${id}/cancel`, {});
  }

  /**
   * Descarga el PDF real desde el backend y dispara la descarga en el
   * navegador. No se genera ningun PDF en el frontend: el backend es
   * la unica fuente de verdad del documento.
   */
  downloadPdf(id: number, invoiceNumber: string): void {
    this.http.get(`${this.baseUrl}/${id}/pdf`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `liquidacion-${invoiceNumber}.pdf`;
        // Firefox exige que el enlace este en el documento para que el
        // click dispare la descarga.
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        link.remove();
        // Se libera la URL en el siguiente ciclo: revocarla de inmediato
        // cancela la descarga en algunos navegadores.
        setTimeout(() => window.URL.revokeObjectURL(url), 0);
      },
      // El interceptor de errores ya avisa al usuario; este error vacio
      // existe para que el fallo no quede como excepcion sin capturar.
      error: () => {},
    });
  }
}
