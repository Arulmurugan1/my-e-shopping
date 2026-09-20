import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AuthStateService } from '../../services/auth-state.service';
import { CustomerProfile, Order, ShopApiService } from '../../services/shop-api.service';
import { NavComponent } from '../../shared/nav/nav.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, NavComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);

  profile: CustomerProfile | null = null;
  orders: Order[] = [];
  status = 'Loading your account...';

  ngOnInit() {
    const userId = this.auth.userId();
    const customerId = this.auth.customerId();
    if (!userId || !customerId) { return; }

    this.api.getProfile(userId).subscribe({ next: response => { this.profile = response.data; }, error: () => { } });

    this.api.getOrders(customerId).subscribe({
      next: response => {
        this.orders = response.data;
        this.status = this.orders.length ? '' : 'No orders yet - head to Order Now to place your first one.';
      },
      error: () => { this.status = 'Could not load your orders.'; }
    });
  }
}
