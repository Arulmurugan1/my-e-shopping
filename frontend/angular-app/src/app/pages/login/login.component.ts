import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStateService } from '../../services/auth-state.service';
import { RegisterDetails, ShopApiService } from '../../services/shop-api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly api = inject(ShopApiService);
  private readonly auth = inject(AuthStateService);
  private readonly router = inject(Router);

  identifier = '';
  password = '';
  status = '';
  showRegister = false;
  signingIn = false;
  reg: RegisterDetails = { email: '', username: '', mobileNumber: '', dateOfBirth: '', gender: '', password: '' };
  readonly today = new Date().toISOString().slice(0, 10);

  login() {
    if (this.signingIn) { return; }
    this.status = '';
    this.signingIn = true;
    this.api.login(this.identifier.trim(), this.password).subscribe({
      next: response => {
        const { token, userId, email, username, role } = response.data;
        this.api.ensureProfile(userId, username || email.split('@')[0] || 'Customer', '').subscribe({
          next: profileResponse => {
            this.auth.setSession(token, userId, profileResponse.data.id, email, role);
            this.router.navigate(['/home']);
          },
          error: () => {
            this.signingIn = false;
            this.status = 'Signed in, but could not set up your customer profile.';
          }
        });
      },
      error: err => {
        this.signingIn = false;
        this.status = this.describe(err, 'Sign-in failed. Check your details and password.');
      }
    });
  }

  private describe(err: { status?: number; error?: { message?: string } }, fallback: string): string {
    if (err?.error?.message) { return err.error.message; }
    if (err?.status === 0) { return 'Cannot reach the server. Make sure the API gateway (port 8080) and auth-service are running.'; }
    if (err?.status && err.status >= 500) { return 'The service is temporarily unavailable. Please try again in a moment.'; }
    return fallback;
  }

  toggleRegister() {
    this.showRegister = !this.showRegister;
    this.status = '';
  }

  register() {
    this.status = '';
    if (!this.reg.gender) { this.status = 'Please select your gender.'; return; }
    if (!this.reg.dateOfBirth) { this.status = 'Please enter your date of birth.'; return; }
    const details: RegisterDetails = {
      ...this.reg,
      email: this.reg.email.trim(),
      username: this.reg.username.trim(),
      mobileNumber: this.reg.mobileNumber.trim()
    };
    this.api.register(details).subscribe({
      next: () => {
        this.status = 'Account created. You can sign in now with your username, email or mobile number.';
        this.showRegister = false;
        this.identifier = details.username;
        this.password = '';
        this.reg = { email: '', username: '', mobileNumber: '', dateOfBirth: '', gender: '', password: '' };
      },
      error: err => { this.status = this.describe(err, 'Registration failed.'); }
    });
  }
}
