import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthStateService);
  const router = inject(Router);
  const loggedIn = auth.validate();
  if (loggedIn && auth.isAdmin()) { return true; }
  router.navigate([loggedIn ? '/home' : '/login']);
  return false;
};
