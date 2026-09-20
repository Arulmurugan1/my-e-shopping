import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthStateService } from '../../services/auth-state.service';
import { Cart, Product, ShopApiService } from '../../services/shop-api.service';
import { NavComponent } from '../../shared/nav/nav.component';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavComponent],
  templateUrl: './products.component.html'
})
export class ProductsComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);

  products: Product[] = [];
  cart: Cart | null = null;
  status = '';
  busyProductId: number | null = null;
  brokenImages: Record<number, boolean> = {};
  search = '';

  get filteredProducts(): Product[] {
    const term = this.search.trim().toLowerCase();
    if (!term) { return this.products; }
    return this.products.filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.sku.toLowerCase().includes(term) ||
      (p.description || '').toLowerCase().includes(term));
  }

  ngOnInit() {
    this.loadProducts();
    this.loadCart();
  }

  loadProducts() {
    this.api.getProducts().subscribe({
      next: response => { this.products = response.data; },
      error: () => { this.status = 'Start Inventory Service to browse products.'; }
    });
  }

  private loadCart() {
    const customerId = this.auth.customerId();
    if (!customerId) { return; }
    this.api.getCart(customerId).subscribe({
      next: response => { this.cart = response.data; },
      error: () => { this.cart = null; }
    });
  }

  quantityOf(product: Product): number {
    return this.cart?.items.find(item => item.productId === product.id)?.quantity ?? 0;
  }

  increase(product: Product) {
    const customerId = this.auth.customerId();
    if (!customerId || this.busyProductId !== null) { return; }
    if (this.quantityOf(product) >= product.availableQuantity) {
      this.status = `Only ${product.availableQuantity} of ${product.name} available.`;
      return;
    }
    this.run(product, this.api.addToCart(customerId, product), `${product.name} added to cart.`);
  }

  decrease(product: Product) {
    const customerId = this.auth.customerId();
    const quantity = this.quantityOf(product);
    if (!customerId || this.busyProductId !== null || quantity <= 0) { return; }
    const request = quantity > 1
      ? this.api.updateCartItem(customerId, product.id, quantity - 1)
      : this.api.removeFromCart(customerId, product.id);
    this.run(product, request, quantity > 1 ? `${product.name} quantity updated.` : `${product.name} removed from cart.`);
  }

  private run(product: Product, request: ReturnType<ShopApiService['addToCart']>, message: string) {
    this.busyProductId = product.id;
    request.subscribe({
      next: response => { this.cart = response.data; this.status = message; this.busyProductId = null; },
      error: err => { this.status = err?.error?.message || 'Cart service is unavailable.'; this.busyProductId = null; }
    });
  }
}
