import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AuthStateService } from '../../services/auth-state.service';
import { Cart, ShopApiService } from '../../services/shop-api.service';
import { NavComponent } from '../../shared/nav/nav.component';
import { PopupComponent } from '../../shared/popup/popup.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, NavComponent, PopupComponent],
  templateUrl: './cart.component.html'
})
export class CartComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);

  cart: Cart | null = null;
  status = '';
  placing = false;

  popupVisible = false;
  popupSuccess = true;
  popupTitle = '';
  popupMessage = '';

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    const customerId = this.auth.customerId();
    if (!customerId) { return; }
    this.api.getCart(customerId).subscribe({
      next: response => { this.cart = response.data; },
      error: () => { this.status = 'Could not load your cart.'; }
    });
  }

  removeItem(productId: number) {
    const customerId = this.auth.customerId();
    if (!customerId) { return; }
    this.api.removeFromCart(customerId, productId).subscribe({
      next: response => { this.cart = response.data; },
      error: () => { this.status = 'Could not remove item.'; }
    });
  }

  placeOrder() {
    const customerId = this.auth.customerId();
    if (!customerId || !this.cart?.items?.length) { return; }
    this.placing = true;
    const lines = this.cart.items.map(item => ({
      productId: item.productId, sku: item.sku, productName: item.productName, unitPrice: item.unitPrice, quantity: item.quantity
    }));
    const correlationId = crypto.randomUUID();
    this.api.placeOrder(customerId, '123 Market Street', lines, correlationId).subscribe({
      next: response => {
        this.placing = false;
        this.popupSuccess = true;
        this.popupTitle = 'Order placed!';
        this.popupMessage = `Order #${response.data.id} was placed successfully. Trace ID: ${correlationId}`;
        this.popupVisible = true;
        this.cart = null;
        this.api.clearCart(customerId, correlationId).subscribe({ error: () => { this.status = 'Order placed, but the cart could not be cleared.'; } });
      },
      error: err => {
        this.placing = false;
        this.popupSuccess = false;
        this.popupTitle = 'Order failed';
        this.popupMessage = err?.error?.message || 'Could not place your order. Please try again.';
        this.popupVisible = true;
      }
    });
  }

  closePopup() {
    this.popupVisible = false;
  }
}
