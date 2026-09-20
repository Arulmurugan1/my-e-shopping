import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent), canActivate: [guestGuard] },
  { path: 'home', loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent), canActivate: [authGuard] },
  { path: 'products', loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent), canActivate: [authGuard] },
  { path: 'cart', loadComponent: () => import('./pages/cart/cart.component').then(m => m.CartComponent), canActivate: [authGuard] },
  { path: 'admin/users', loadComponent: () => import('./pages/admin/users/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [adminGuard] },
  { path: 'admin/products', loadComponent: () => import('./pages/admin/products/admin-products.component').then(m => m.AdminProductsComponent), canActivate: [adminGuard] },
  { path: 'admin/customers', loadComponent: () => import('./pages/admin/customers/admin-customers.component').then(m => m.AdminCustomersComponent), canActivate: [adminGuard] },
  { path: 'admin', pathMatch: 'full', redirectTo: 'admin/users' },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' }
];
