import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginResponse } from '../models/user.model';

const STORAGE_KEY = 'elpuntazo_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<AuthenticatedUser | null>(this.restoreSession());

  readonly currentUser = computed(() => this.currentUserSignal());
  readonly isLoggedIn = computed(() => this.currentUserSignal() !== null);
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap((response) => this.persistSession(response)),
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.currentUserSignal.set(null);
  }

  // Si lo guardado en localStorage quedo corrupto, JSON.parse lanza una
  // excepcion. Sin este try/catch esa excepcion rompia TODAS las peticiones
  // (el interceptor llama a getToken en cada una) y la app quedaba muerta
  // sin forma de recuperarse salvo borrando los datos del navegador.
  getToken(): string | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      return (JSON.parse(raw) as LoginResponse).token ?? null;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  private persistSession(response: LoginResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
    this.currentUserSignal.set({
      userId: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    });
  }

  private restoreSession(): AuthenticatedUser | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw) as LoginResponse;
      return { userId: parsed.userId, name: parsed.name, email: parsed.email, role: parsed.role };
    } catch {
      return null;
    }
  }
}
