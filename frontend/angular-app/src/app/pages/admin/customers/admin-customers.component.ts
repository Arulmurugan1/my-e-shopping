import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthStateService } from '../../../services/auth-state.service';
import { ShopApiService } from '../../../services/shop-api.service';
import { NavComponent } from '../../../shared/nav/nav.component';

type CustomerRow = { customerId: number; name: string; email: string; orderCount: number; totalAmount: number; lastOrderAt: string };

@Component({
  selector: 'app-admin-customers',
  standalone: true,
  imports: [CommonModule, NavComponent],
  templateUrl: './admin-customers.component.html'
})
export class AdminCustomersComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);
  private readonly router = inject(Router);

  rows: CustomerRow[] = [];
  status = 'Loading customers...';

  get grandTotal(): number { return this.rows.reduce((sum, r) => sum + r.totalAmount, 0); }
  get totalOrders(): number { return this.rows.reduce((sum, r) => sum + r.orderCount, 0); }

  ngOnInit() {
    forkJoin({
      totals: this.api.adminCustomerTotals(),
      profiles: this.api.adminListCustomers(),
      users: this.api.adminListUsers()
    }).subscribe({
      next: ({ totals, profiles, users }) => {
        const profileById = new Map(profiles.data.map(p => [p.id, p]));
        const emailByUserId = new Map(users.data.map(u => [u.id, u.email]));
        this.rows = totals.data
          .map(t => {
            const profile = profileById.get(t.customerId);
            const email = profile ? (emailByUserId.get(profile.userId) || '-') : '-';
            const name = profile ? `${profile.firstName} ${profile.lastName}`.trim() : `Customer #${t.customerId}`;
            return { customerId: t.customerId, name, email, orderCount: t.orderCount, totalAmount: t.totalAmount, lastOrderAt: t.lastOrderAt };
          })
          .sort((a, b) => b.totalAmount - a.totalAmount);
        this.status = this.rows.length ? '' : 'No customers have placed orders yet.';
      },
      error: err => {
        if (err?.status === 403 || err?.status === 401) { this.auth.downgrade(); this.router.navigate(['/home']); return; }
        this.status = 'Could not load customers. Check that order-service, customer-service and auth-service are running.';
      }
    });
  }
}
