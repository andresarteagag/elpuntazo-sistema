import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

/**
 * Nunca deja pasar un error tecnico crudo hacia la pantalla.
 * Si el backend ya mando un mensaje claro (ver GlobalExceptionHandler
 * en el backend), se usa ese mismo mensaje.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        auth.logout();
        router.navigate(['/login']);
        notifications.show('Tu sesion expiro. Inicia sesion nuevamente.', 'error');
      } else if (error.status === 403) {
        notifications.show('No tienes permiso para realizar esta accion.', 'error');
      } else if (error.error instanceof Blob) {
        // Las descargas de PDF usan responseType 'blob', asi que el cuerpo
        // del error tambien llega como Blob. Hay que leerlo para poder
        // mostrar el mensaje real del backend en vez de uno generico.
        error.error.text().then((text) => {
          notifications.show(extractMessage(text), 'error');
        });
      } else {
        const backendMessage = error.error?.message;
        notifications.show(backendMessage || 'Ocurrio un problema inesperado. Intenta nuevamente.', 'error');
      }

      return throwError(() => error);
    }),
  );
};

function extractMessage(rawBody: string): string {
  const fallback = 'Ocurrio un problema inesperado. Intenta nuevamente.';
  try {
    return (JSON.parse(rawBody) as { message?: string }).message ?? fallback;
  } catch {
    return fallback;
  }
}
