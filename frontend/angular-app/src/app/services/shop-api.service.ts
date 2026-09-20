import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type Product = { id: number; sku: string; name: string; description: string; price: number; availableQuantity: number; stockQuantity?: number; reservedQuantity?: number; imageUrl?: string | null };
export type CartItem = { productId: number; sku: string; productName: string; quantity: number; unitPrice: number };
export type Cart = { totalItems: number; totalAmount: number; items: CartItem[] };
export type CustomerProfile = { id: number; userId: string; firstName: string; lastName: string; phoneNumber?: string };
export type OrderLine = { productId: number; sku: string; productName: string; unitPrice: number; quantity: number };
export type Order = { id: number; customerId: number; shippingAddress: string; status: string; totalAmount: number; createdAt: string; updatedAt?: string; lines?: OrderLine[] };
export type CancelResult = { orderId: number; outcome: 'CANCELLED_AND_REFUNDED' | 'RETURNED_AND_REFUNDED'; orderStatus: string; refundAmount: number };

export type RegisterDetails = { email: string; username: string; mobileNumber: string; dateOfBirth: string; gender: string; password: string };
export type AdminUser = { id: string; email: string; username?: string | null; mobileNumber?: string | null; role: string; active: boolean; createdAt: string };
export type CustomerOrderTotal = { customerId: number; orderCount: number; totalAmount: number; lastOrderAt: string };
export type NewProduct = { sku: string; name: string; description: string; imageUrl: string; price: number; initialStock: number };

@Injectable({ providedIn: 'root' })
export class ShopApiService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8080/api/v1';

  login(identifier: string, password: string) {
    return this.http.post<{ data: { token: string; userId: string; email: string; username?: string; role: string } }>(`${this.api}/auth/login`, { identifier, password });
  }

  register(details: RegisterDetails) {
    return this.http.post<{ data: { token: string } }>(`${this.api}/auth/register`, details);
  }

  ensureProfile(userId: string, firstName: string, lastName: string) {
    return this.http.post<{ data: CustomerProfile }>(`${this.api}/customers/profile`, { userId, firstName, lastName });
  }

  getProfile(userId: string) {
    return this.http.get<{ data: CustomerProfile }>(`${this.api}/customers/${userId}/profile`);
  }

  getProducts() {
    return this.http.get<{ data: Product[] }>(`${this.api}/products`);
  }

  getCart(customerId: number) {
    return this.http.get<{ data: Cart }>(`${this.api}/cart/${customerId}`);
  }

  addToCart(customerId: number, product: Product) {
    return this.http.post<{ data: Cart }>(`${this.api}/cart/${customerId}/items`, {
      productId: product.id, sku: product.sku, productName: product.name, unitPrice: product.price, quantity: 1
    });
  }

  updateCartItem(customerId: number, productId: number, quantity: number) {
    return this.http.put<{ data: Cart }>(`${this.api}/cart/${customerId}/items/${productId}?quantity=${quantity}`, {});
  }

  removeFromCart(customerId: number, productId: number) {
    return this.http.delete<{ data: Cart }>(`${this.api}/cart/${customerId}/items/${productId}`);
  }

  clearCart(customerId: number, correlationId?: string) {
    const headers = correlationId ? { 'X-Correlation-ID': correlationId } : undefined;
    return this.http.delete<{ data: Cart }>(`${this.api}/cart/${customerId}`, { headers });
  }

  placeOrder(customerId: number, shippingAddress: string, lines: OrderLine[], correlationId?: string) {
    const headers = correlationId ? { 'X-Correlation-ID': correlationId } : undefined;
    return this.http.post<{ data: Order }>(`${this.api}/orders`, { customerId, shippingAddress, lines }, { headers });
  }

  getOrders(customerId: number) {
    return this.http.get<{ data: Order[] }>(`${this.api}/orders/customer/${customerId}`);
  }

  downloadInvoice(orderId: number) {
    return this.http.get(`${this.api}/invoices/orders/${orderId}/pdf`, { responseType: 'blob' });
  }

  cancelOrder(orderId: number, reason = 'Cancelled by customer') {
    return this.http.post<{ data: CancelResult }>(`${this.api}/returns/orders/${orderId}/cancel`, { reason });
  }

  adminListUsers() {
    return this.http.get<{ data: AdminUser[] }>(`${this.api}/auth/admin/users`);
  }

  adminSetUserActive(id: string, active: boolean) {
    return this.http.patch<{ data: AdminUser }>(`${this.api}/auth/admin/users/${id}/active`, { active });
  }

  adminSetUserRole(id: string, role: string) {
    return this.http.patch<{ data: AdminUser }>(`${this.api}/auth/admin/users/${id}/role`, { role });
  }

  adminCreateProduct(product: NewProduct) {
    return this.http.post<{ data: Product }>(`${this.api}/products`, product);
  }

  adminSetProductImage(productId: number, imageUrl: string) {
    return this.http.put<{ data: Product }>(`${this.api}/products/${productId}/image`, { imageUrl });
  }

  adminAddStock(productId: number, quantity: number) {
    return this.http.post<{ data: Product }>(`${this.api}/inventory/${productId}/add`, { quantity });
  }

  adminDeactivateProduct(productId: number) {
    return this.http.delete<{ message: string }>(`${this.api}/products/${productId}`);
  }

  adminCustomerTotals() {
    return this.http.get<{ data: CustomerOrderTotal[] }>(`${this.api}/orders/admin/customer-totals`);
  }

  adminListCustomers() {
    return this.http.get<{ data: CustomerProfile[] }>(`${this.api}/customers`);
  }
}
