import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateSellerRequest, Seller, UpdateSellerRequest } from '../models/seller.model';

@Injectable({ providedIn: 'root' })
export class SellerService {
  private readonly baseUrl = `${environment.apiUrl}/sellers`;

  constructor(private http: HttpClient) {}

  listAll(): Observable<Seller[]> {
    return this.http.get<Seller[]>(this.baseUrl);
  }

  create(request: CreateSellerRequest): Observable<Seller> {
    return this.http.post<Seller>(this.baseUrl, request);
  }

  update(id: number, request: UpdateSellerRequest): Observable<Seller> {
    return this.http.put<Seller>(`${this.baseUrl}/${id}`, request);
  }

  setActive(id: number, active: boolean): Observable<Seller> {
    return this.http.patch<Seller>(`${this.baseUrl}/${id}/status`, { active });
  }
}
