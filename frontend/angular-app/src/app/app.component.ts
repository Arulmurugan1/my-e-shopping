import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Product = { id: number; sku: string; name: string; description: string; price: number; availableQuantity: number };
type Cart = { totalItems: number; totalAmount: number; items: { productName: string; quantity: number; unitPrice: number }[] };

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './app.component.html' })
export class AppComponent {
  private readonly http = inject(HttpClient);
  readonly api = 'http://localhost:8080/api/v1';
  products: Product[] = []; cart: Cart | null = null; customerId = 1001; status = 'Ready when you are.'; email = ''; password = '';
  ngOnInit() { this.loadProducts(); }
  loadProducts() { this.http.get<{ data: Product[] }>(`${this.api}/products`).subscribe({ next: response => this.products = response.data, error: () => this.status = 'Start Inventory Service to browse products.' }); }
  add(product: Product) { this.http.post<{ data: Cart }>(`${this.api}/cart/${this.customerId}/items`, { productId: product.id, sku: product.sku, productName: product.name, unitPrice: product.price, quantity: 1 }).subscribe({ next: response => { this.cart = response.data; this.status = `${product.name} added to cart.`; }, error: () => this.status = 'Cart service is unavailable.' }); }
  login() { this.http.post<{ data: { token: string } }>(`${this.api}/auth/login`, { email: this.email, password: this.password }).subscribe({ next: response => { localStorage.setItem('accessToken', response.data.token); this.status = 'Signed in successfully.'; }, error: () => this.status = 'Sign-in failed.' }); }
}
