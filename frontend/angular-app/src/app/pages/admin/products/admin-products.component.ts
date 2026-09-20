import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NewProduct, Product, ShopApiService } from '../../../services/shop-api.service';
import { NavComponent } from '../../../shared/nav/nav.component';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, FormsModule, NavComponent],
  templateUrl: './admin-products.component.html'
})
export class AdminProductsComponent implements OnInit {
  private readonly api = inject(ShopApiService);

  products: Product[] = [];
  form: NewProduct = { sku: '', name: '', description: '', imageUrl: '', price: 0, initialStock: 0 };
  status = '';
  saving = false;

  ngOnInit() { this.load(); }

  load() {
    this.api.getProducts().subscribe({
      next: response => { this.products = response.data; },
      error: () => { this.status = 'Could not load SKUs. Is inventory-service running?'; }
    });
  }

  add() {
    if (!this.form.sku.trim() || !this.form.name.trim()) { this.status = 'SKU and name are required.'; return; }
    this.saving = true;
    this.api.adminCreateProduct({ ...this.form, sku: this.form.sku.trim(), name: this.form.name.trim() }).subscribe({
      next: response => {
        this.saving = false;
        this.status = `SKU ${response.data.sku} added.`;
        this.form = { sku: '', name: '', description: '', imageUrl: '', price: 0, initialStock: 0 };
        this.load();
      },
      error: err => {
        this.saving = false;
        if (err?.error?.message) { this.status = err.error.message; }
        else if (err?.status === 400) { this.status = 'Check the form: SKU and name are required, and price and stock cannot be negative.'; }
        else if (err?.status >= 500) { this.status = 'The server failed while saving the SKU. Check the inventory-service logs.'; }
        else { this.status = 'Could not add the SKU. Is inventory-service running?'; }
      }
    });
  }

  restock: Record<number, number | null> = {};
  restockingId: number | null = null;
  imageEdits: Record<number, string> = {};
  savingImageId: number | null = null;

  saveImage(product: Product) {
    const imageUrl = (this.imageEdits[product.id] ?? product.imageUrl ?? '').trim();
    if (imageUrl && !/^https?:\/\/\S+$/i.test(imageUrl)) { this.status = 'Image URL must start with http:// or https://'; return; }
    this.savingImageId = product.id;
    this.api.adminSetProductImage(product.id, imageUrl).subscribe({
      next: () => {
        this.savingImageId = null;
        delete this.imageEdits[product.id];
        this.status = imageUrl ? `Image saved for ${product.sku}.` : `Image removed from ${product.sku}.`;
        this.load();
      },
      error: err => { this.savingImageId = null; this.status = err?.error?.message || 'Could not save the image URL.'; }
    });
  }

  addStock(product: Product) {
    const quantity = Number(this.restock[product.id]);
    if (!Number.isInteger(quantity) || quantity <= 0) { this.status = 'Enter a whole number of units greater than 0.'; return; }
    this.restockingId = product.id;
    this.api.adminAddStock(product.id, quantity).subscribe({
      next: response => {
        this.restockingId = null;
        this.restock[product.id] = null;
        this.status = `Added ${quantity} unit(s) to ${product.sku}. Available now: ${response.data.availableQuantity}.`;
        this.load();
      },
      error: err => { this.restockingId = null; this.status = err?.error?.message || 'Could not add stock.'; }
    });
  }

  deactivate(product: Product) {
    if (!confirm(`Deactivate ${product.sku} - ${product.name}? It will disappear from the catalog.`)) { return; }
    this.api.adminDeactivateProduct(product.id).subscribe({
      next: () => { this.status = `SKU ${product.sku} deactivated.`; this.load(); },
      error: err => { this.status = err?.error?.message || 'Could not deactivate the SKU.'; }
    });
  }
}
