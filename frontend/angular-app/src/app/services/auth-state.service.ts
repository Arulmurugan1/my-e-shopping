import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly _customerId = signal<number | null>(null);
  private readonly _userId = signal<string | null>(null);
  private readonly _email = signal<string>('');
  private readonly _role = signal<string>('');

  readonly customerId = this._customerId.asReadonly();
  readonly userId = this._userId.asReadonly();
  readonly email = this._email.asReadonly();
  readonly role = this._role.asReadonly();
  readonly isLoggedIn = computed(() => this._customerId() !== null);
  readonly isAdmin = computed(() => this._role() === 'ADMIN' || this._role() === 'SUPER_ADMIN');
  readonly isSuperAdmin = computed(() => this._role() === 'SUPER_ADMIN');

  constructor() {
    this.restore();
  }

  /** True while the stored token is still valid; clears the session (and returns false) once it has expired. */
  validate(): boolean {
    const token = localStorage.getItem('accessToken');
    if (!token) { return this.isLoggedIn(); }
    if (this.isExpired(token)) {
      this.clear();
      return false;
    }
    return this.isLoggedIn();
  }

  /** Called when the server refuses an admin call: stop showing admin options. */
  downgrade() {
    localStorage.setItem('userRole', 'CUSTOMER');
    this._role.set('CUSTOMER');
  }

  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now();
    } catch {
      return false;
    }
  }

  private restore() {
    const token = localStorage.getItem('accessToken');
    if (token && this.isExpired(token)) {
      ['accessToken', 'userId', 'customerId', 'userEmail', 'userRole'].forEach(key => localStorage.removeItem(key));
      return;
    }
    const userId = localStorage.getItem('userId');
    const customerId = localStorage.getItem('customerId');
    const email = localStorage.getItem('userEmail');
    const role = localStorage.getItem('userRole');
    if (token && userId && customerId) {
      this._userId.set(userId);
      this._customerId.set(Number(customerId));
      this._email.set(email || '');
      this._role.set(role || 'CUSTOMER');
    }
  }

  setSession(token: string, userId: string, customerId: number, email: string, role: string) {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('userId', userId);
    localStorage.setItem('customerId', String(customerId));
    localStorage.setItem('userEmail', email);
    localStorage.setItem('userRole', role);
    this._userId.set(userId);
    this._customerId.set(customerId);
    this._email.set(email);
    this._role.set(role);
  }

  clear() {
    ['accessToken', 'userId', 'customerId', 'userEmail', 'userRole'].forEach(key => localStorage.removeItem(key));
    this._userId.set(null);
    this._customerId.set(null);
    this._email.set('');
    this._role.set('');
  }
}
