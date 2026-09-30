export interface Client {
  id: number;
  name: string;
  identification: string | null;
  phone: string | null;
  email: string | null;
  active: boolean;
}

export interface ClientRequest {
  name: string;
  identification?: string;
  phone?: string;
  email?: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
