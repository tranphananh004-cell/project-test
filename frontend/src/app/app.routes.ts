import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent),
        canActivate: [permissionGuard],
        data: { permission: 'USER_READ' }
      },
      {
        path: 'roles',
        loadComponent: () => import('./features/roles/roles.component').then(m => m.RolesComponent),
        canActivate: [permissionGuard],
        data: { permission: 'ROLE_READ' }
      },
      {
        path: 'permissions',
        loadComponent: () => import('./features/permissions/permissions.component').then(m => m.PermissionsComponent),
        canActivate: [permissionGuard],
        data: { permission: 'PERMISSION_READ' }
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
