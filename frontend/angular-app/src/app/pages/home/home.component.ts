import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { AuthStateService } from '../../services/auth-state.service';
import { CustomerProfile, Order, ShopApiService } from '../../services/shop-api.service';
import { NavComponent } from '../../shared/nav/nav.component';
import { PopupComponent } from '../../shared/popup/popup.component';

const TWO_DAYS_MS = 2 * 24 * 60 * 60 * 1000;
const CLOSED_STATUSES = ['CANCELLED', 'RETURNED', 'REFUNDED'];
const IN_PROGRESS_STATUSES = ['ORDERED', 'PICKING_PENDING', 'PICKING_IN_PROGRESS', 'PICKED', 'SHIPPING_PENDING'];

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, NavComponent, PopupComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);

  profile: CustomerProfile | null = null;
  orders: Order[] = [];
  status = 'Loading your account...';
  downloadingId: number | null = null;
  cancellingId: number | null = null;

  popupVisible = false;
  popupSuccess = true;
  popupTitle = '';
  popupMessage = '';

  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  ngOnInit() {
    const userId = this.auth.userId();
    if (!userId) { return; }
    this.api.getProfile(userId).subscribe({ next: response => { this.profile = response.data; }, error: () => { } });
    this.loadOrders();
    // While an order is still moving through picking/shipping, refresh so the status updates on its own.
    this.refreshTimer = setInterval(() => {
      if (this.orders.some(o => IN_PROGRESS_STATUSES.includes(o.status))) { this.loadOrders(true); }
    }, 5000);
  }

  ngOnDestroy() {
    if (this.refreshTimer) { clearInterval(this.refreshTimer); }
  }

  loadOrders(quiet = false) {
    const customerId = this.auth.customerId();
    if (!customerId) { return; }
    this.api.getOrders(customerId).subscribe({
      next: response => {
        this.orders = response.data;
        this.status = this.orders.length ? '' : 'No orders yet - head to Order Now to place your first one.';
      },
      error: () => { if (!quiet) { this.status = 'Could not load your orders.'; } }
    });
  }

  statusLabel(status: string): string {
    return status.replace(/_/g, ' ');
  }

  isClosed(order: Order): boolean {
    return CLOSED_STATUSES.includes(order.status);
  }

  /** Cancel is offered from order creation until delivery, and for 2 days after delivery (as a return). */
  canCancel(order: Order): boolean {
    if (this.isClosed(order)) { return false; }
    if (order.status === 'DELIVERED') {
      const deliveredAt = new Date((order.updatedAt || '').slice(0, 23)).getTime();
      return !Number.isNaN(deliveredAt) && Date.now() - deliveredAt <= TWO_DAYS_MS;
    }
    return true;
  }

  downloadInvoice(order: Order) {
    this.downloadingId = order.id;
    this.api.downloadInvoice(order.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `invoice-order-${order.id}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        this.downloadingId = null;
      },
      error: async err => {
        this.downloadingId = null;
        this.showPopup(false, 'Invoice unavailable', await this.errorText(err, 'Could not generate the invoice. Please try again.'));
      }
    });
  }

  cancelOrder(order: Order) {
    const delivered = order.status === 'DELIVERED';
    const question = delivered
      ? `Return order #${order.id}? It will be marked as returned and refunded.`
      : `Cancel order #${order.id}? It will be cancelled and refunded.`;
    if (!confirm(question)) { return; }
    this.cancellingId = order.id;
    this.api.cancelOrder(order.id).subscribe({
      next: response => {
        this.cancellingId = null;
        const returned = response.data.outcome === 'RETURNED_AND_REFUNDED';
        this.showPopup(true, returned ? 'Order returned' : 'Order cancelled',
          `Order #${order.id} was ${returned ? 'returned' : 'cancelled'} and $${response.data.refundAmount.toFixed(2)} was refunded.`);
        this.loadOrders();
      },
      error: async err => {
        this.cancellingId = null;
        this.showPopup(false, 'Could not cancel', await this.errorText(err, 'The order could not be cancelled. Please try again.'));
        this.loadOrders();
      }
    });
  }

  closePopup() { this.popupVisible = false; }

  private showPopup(success: boolean, title: string, message: string) {
    this.popupSuccess = success;
    this.popupTitle = title;
    this.popupMessage = message;
    this.popupVisible = true;
  }

  /** Error bodies for blob downloads arrive as a Blob, so read the text before looking for a message. */
  private async errorText(err: { error?: unknown; status?: number }, fallback: string): Promise<string> {
    try {
      const body = err.error instanceof Blob ? JSON.parse(await err.error.text()) : err.error;
      const message = (body as { message?: string } | null)?.message;
      if (message) { return message; }
    } catch { /* not JSON */ }
    if (err.status === 0) { return 'Cannot reach the server. Make sure the services are running.'; }
    return fallback;
  }
}
