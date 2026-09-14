import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Product = { id: number; sku: string; name: string; description: string; price: number; availableQuantity: number };
type Cart = { totalItems: number; totalAmount: number; items: { productId: number; sku: string; productName: string; quantity: number; unitPrice: number }[] };

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './app.component.html' })
export class AppComponent {
  private readonly http = inject(HttpClient);
  readonly api = 'http://localhost:8080/api/v1';
  products: Product[] = []; cart: Cart | null = null; customerId = 1001; status = 'Ready when you are.'; email = ''; password = '';
  showRegister = false; regEmail = ''; regPassword = '';
  ngOnInit() { this.loadProducts(); }
  loadProducts() { this.http.get<{ data: Product[] }>(`${this.api}/products`).subscribe({ next: response => this.products = response.data, error: () => this.status = 'Start Inventory Service to browse products.' }); }
  add(product: Product) { this.http.post<{ data: Cart }>(`${this.api}/cart/${this.customerId}/items`, { productId: product.id, sku: product.sku, productName: product.name, unitPrice: product.price, quantity: 1 }).subscribe({ next: response => { this.cart = response.data; this.status = `${product.name} added to cart.`; }, error: () => this.status = 'Cart service is unavailable.' }); }
  placeOrder() {
    if (!this.cart?.items?.length) { this.status = 'Your cart is empty.'; return; }
    const lines = this.cart.items.map(item => ({ productId: item.productId, sku: item.sku, productName: item.productName, unitPrice: item.unitPrice, quantity: item.quantity }));
    this.http.post<{ data: { id: number } }>(`${this.api}/orders`, { customerId: this.customerId, shippingAddress: '123 Market Street', lines }).subscribe({ next: response => { this.status = `Order #${response.data.id} placed successfully.`; this.cart = null; }, error: () => this.status = 'Could not place order.' });
  }
  login() { this.http.post<{ data: { token: string } }>(`${this.api}/auth/login`, { email: this.email, password: this.password }).subscribe({ next: response => { localStorage.setItem('accessToken', response.data.token); this.status = 'Signed in successfully.'; }, error: () => this.status = 'Sign-in failed.' }); }
  toggleRegister() { this.showRegister = !this.showRegister; }
  register() { this.http.post<{ data: { token: string } }>(`${this.api}/auth/register`, { email: this.regEmail, password: this.regPassword }).subscribe({ next: () => { this.status = 'Account created. You can sign in now.'; this.showRegister = false; this.email = this.regEmail; this.regEmail = ''; this.regPassword = ''; }, error: err => this.status = err?.error?.message || 'Registration failed.' }); }
}
