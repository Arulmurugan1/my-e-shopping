import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStateService } from '../../../services/auth-state.service';
import { AdminUser, ShopApiService } from '../../../services/shop-api.service';
import { NavComponent } from '../../../shared/nav/nav.component';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, NavComponent],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);
  private readonly router = inject(Router);
  readonly myEmail = this.auth.email;

  users: AdminUser[] = [];
  filter: 'all' | 'active' | 'inactive' = 'all';
  status = 'Loading users...';

  get visibleUsers(): AdminUser[] {
    if (this.filter === 'active') { return this.users.filter(u => u.active); }
    if (this.filter === 'inactive') { return this.users.filter(u => !u.active); }
    return this.users;
  }

  get activeCount(): number { return this.users.filter(u => u.active).length; }
  get inactiveCount(): number { return this.users.length - this.activeCount; }

  ngOnInit() { this.load(); }

  load() {
    this.api.adminListUsers().subscribe({
      next: response => { this.users = response.data; this.status = this.users.length ? '' : 'No users found.'; },
      error: err => {
        if (err?.status === 403 || err?.status === 401) { this.auth.downgrade(); this.router.navigate(['/home']); return; }
        this.status = 'Could not load users.';
      }
    });
  }

  toggleActive(user: AdminUser) {
    this.api.adminSetUserActive(user.id, !user.active).subscribe({
      next: response => { this.replace(response.data); this.status = `${user.email} is now ${response.data.active ? 'active' : 'inactive'}.`; },
      error: err => { this.status = err?.error?.message || 'Could not update the user.'; }
    });
  }

  toggleAdmin(user: AdminUser) {
    const role = user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN';
    this.api.adminSetUserRole(user.id, role).subscribe({
      next: response => { this.replace(response.data); this.status = `${user.email} is now ${response.data.role}.`; },
      error: err => { this.status = err?.error?.message || 'Could not update the role.'; }
    });
  }

  isMe(user: AdminUser): boolean {
    return user.email.toLowerCase() === this.myEmail().toLowerCase();
  }

  get iAmSuperAdmin(): boolean { return this.auth.isSuperAdmin(); }

  /** Super admin can activate/deactivate anyone but themselves; admins only customers. Nobody can touch a super admin. */
  canToggleActive(user: AdminUser): boolean {
    if (this.isMe(user) || user.role === 'SUPER_ADMIN') { return false; }
    return this.iAmSuperAdmin || user.role === 'CUSTOMER';
  }

  /** Only the super admin can promote/demote admins. */
  canChangeRole(user: AdminUser): boolean {
    return this.iAmSuperAdmin && !this.isMe(user) && user.role !== 'SUPER_ADMIN';
  }

  roleLabel(role: string): string { return role.replace('_', ' '); }

  private replace(updated: AdminUser) {
    this.users = this.users.map(u => u.id === updated.id ? updated : u);
  }
}
