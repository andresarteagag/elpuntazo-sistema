export type InvoiceStatus = 'ACTIVA' | 'ANULADA';

export interface Invoice {
  id: number;
  invoiceNumber: string;
  clientName: string;
  sellerId: number;
  sellerName: string;
  baseValue: number;
  discountPercentage: number;
  discountValue: number;
  valueAfterDiscount: number;
  shippingValue: number;
  shippingAssumedByCompany: number;
  warrantyDeduction: number;
  totalValue: number;
  status: InvoiceStatus;
  createdAt: string;
}

export interface InvoiceRequest {
  clientName: string;
  baseValue: number;
  discountPercentage: number;
  shippingValue: number;
  shippingAssumedByCompany: number;
  warrantyDeduction: number;
}

export interface InvoiceFilters {
  sellerId?: number;
  invoiceNumber?: string;
  status?: InvoiceStatus;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}
