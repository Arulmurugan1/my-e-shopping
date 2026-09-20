import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStateService } from '../../services/auth-state.service';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './nav.component.html'
})
export class NavComponent {
  private readonly auth = inject(AuthStateService);
  private readonly router = inject(Router);
  readonly email = this.auth.email;
  readonly isAdmin = this.auth.isAdmin;
  readonly roleLabel = () => this.auth.role().replace('_', ' ');

  logout() {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
