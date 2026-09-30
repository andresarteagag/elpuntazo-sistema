import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Client, ClientRequest, PageResult } from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly baseUrl = `${environment.apiUrl}/clients`;

  constructor(private http: HttpClient) {}

  search(term: string, page = 0, size = 20): Observable<PageResult<Client>> {
    return this.http.get<PageResult<Client>>(this.baseUrl, {
      params: { search: term ?? '', page, size },
    });
  }

  create(request: ClientRequest): Observable<Client> {
    return this.http.post<Client>(this.baseUrl, request);
  }

  update(id: number, request: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/${id}`, request);
  }

  setActive(id: number, active: boolean): Observable<Client> {
    return this.http.patch<Client>(`${this.baseUrl}/${id}/status`, { active });
  }
}
