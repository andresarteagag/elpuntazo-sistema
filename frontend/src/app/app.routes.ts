import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout.component').then((m) => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'facturas' },
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'facturas/nueva',
        loadComponent: () =>
          import('./pages/nueva-factura/nueva-factura.component').then((m) => m.NuevaFacturaComponent),
      },
      {
        path: 'facturas',
        loadComponent: () => import('./pages/historial/historial.component').then((m) => m.HistorialComponent),
      },
      {
        path: 'clientes',
        canActivate: [adminGuard],
        loadComponent: () => import('./pages/clientes/clientes.component').then((m) => m.ClientesComponent),
      },
      {
        path: 'vendedores',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/vendedores/vendedores.component').then((m) => m.VendedoresComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
